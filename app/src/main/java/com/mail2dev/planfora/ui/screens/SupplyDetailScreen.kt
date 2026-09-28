package com.mail2dev.planfora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.ui.components.InlineAudioPlayer
import com.mail2dev.planfora.ui.logs.LogsViewModel
import com.mail2dev.planfora.ui.navigation.Screen
import com.mail2dev.planfora.ui.supplies.SuppliesViewModel
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplyDetailScreen(
    supplyId: Long,
    navController: NavController,
    viewModel: SuppliesViewModel,
    addSupplyViewModel: com.mail2dev.planfora.ui.supplies.AddSupplyViewModel,
    logsViewModel: LogsViewModel
) {
    val supplies by viewModel.supplies.collectAsState()
    val supply = supplies.find { it.id == supplyId }
    val showEditSheet by viewModel.showAddBottomSheet.collectAsState()
    
    val customFieldValues by logsViewModel.getCustomFieldValues(supplyId).collectAsState(emptyList())
    val customFieldDefinitions by logsViewModel.getCustomFieldDefinitions(com.mail2dev.planfora.data.local.entity.FieldTargetType.SUPPLY_CATEGORY, supply?.category ?: "").collectAsState(emptyList())

    val allLogs by logsViewModel.allLogs.collectAsState()
    val supplyLogs = allLogs.filter { it.supplyId == supplyId }.sortedByDescending { it.timestamp }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFinalizeDialog by remember { mutableStateOf(false) }
    var yieldInput by remember { mutableStateOf(supply?.currentVolume?.toString() ?: "") }
    var logToDelete by remember { mutableStateOf<com.mail2dev.planfora.data.local.entity.JournalLogEntity?>(null) }

    if (showFinalizeDialog && supply != null) {
        AlertDialog(
            onDismissRequest = { showFinalizeDialog = false },
            title = { Text("Finalize DIY Batch", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter the total volume yielded from this batch. This will move the product to your main Inventory Stock.", color = Color.LightGray)
                    OutlinedTextField(
                        value = yieldInput,
                        onValueChange = { yieldInput = it },
                        label = { Text("Yield Volume (${supply.unit})") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val volume = yieldInput.toDoubleOrNull() ?: 0.0
                        viewModel.finalizeBatch(supply, volume)
                        showFinalizeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Finalize & Store") }
            },
            dismissButton = {
                TextButton(onClick = { showFinalizeDialog = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF1E2120)
        )
    }

    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Delete Log Entry?", color = Color.White) },
            text = { Text("Are you sure you want to remove this log? This cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        logsViewModel.deleteLog(logToDelete!!)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF1E2120)
        )
    }

    if (showDeleteConfirm && supply != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Product?", color = Color.White) },
            text = { Text("Are you sure you want to delete '${supply.name}'? This will not delete the history of applications, but the product link will be removed. This cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSupply(supply)
                        showDeleteConfirm = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = Color.Gray) }
            },
            containerColor = Color(0xFF1E2120)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Passport", color = Color.White, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2120))
                    ) {
                        if (supply?.category == "DIY" && !supply.isArchived) {
                            DropdownMenuItem(
                                text = { Text("Finalize Batch", color = MaterialTheme.colorScheme.primary) },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { 
                                    showMenu = false
                                    showFinalizeDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Update Progress", color = com.mail2dev.planfora.ui.theme.SageGreen) },
                                leadingIcon = { Icon(Icons.Default.PendingActions, null, tint = com.mail2dev.planfora.ui.theme.SageGreen) },
                                onClick = { 
                                    showMenu = false
                                    addSupplyViewModel.loadSupply(supplyId, isProductionUpdate = true)
                                    viewModel.setShowAddBottomSheet(true)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Edit Profile", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                addSupplyViewModel.loadSupply(supplyId, isProductionUpdate = false)
                                viewModel.setShowAddBottomSheet(true)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = { 
                                showMenu = false
                                showDeleteConfirm = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        if (supply == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Product not found", color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    ProductHeaderCard(supply, customFieldValues, customFieldDefinitions)
                }

                if (supply.category == "DIY" && !supply.isArchived) {
                    item {
                        ProductionDashboard(supply)
                    }
                }

                if ((supply.phiDays ?: 0) > 0) {
                    item {
                        SafetyAlertBanner(supply.phiDays!!)
                    }
                }

                item {
                    Text(
                        text = "Linked Application History",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (supplyLogs.isEmpty()) {
                    item {
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No research notes recorded yet.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(supplyLogs.reversed()) { log ->
                        ResearchNoteEntry(
                            log = log,
                            startDate = supply.startDate,
                            use24Hour = false, // Hardcoded for build fix
                            onDelete = { logToDelete = log },
                            onEdit = { navController.navigate(Screen.NewLog.createRoute(editingLogId = log.id)) }
                        )
                    }
                }
            }
        }
    }

    if (showEditSheet) {
        com.mail2dev.planfora.ui.supplies.AddSupplyScreen(
            viewModel = addSupplyViewModel,
            onDismiss = { viewModel.setShowAddBottomSheet(false) }
        )
    }
}

@Composable
fun ResearchNoteEntry(
    log: com.mail2dev.planfora.data.local.entity.JournalLogEntity,
    startDate: Long,
    use24Hour: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dayNumber = ((log.timestamp - startDate) / (24L * 60 * 60 * 1000)).coerceAtLeast(0) + 1
    val timeStr = com.mail2dev.planfora.util.TimeFormatter.formatTime(log.timestamp, use24Hour)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(56.dp)
        ) {
            Surface(
                color = if (dayNumber == 1L) com.mail2dev.planfora.ui.theme.ForestGreen else Color.DarkGray,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "D${dayNumber.toString().padStart(2, '0')}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }

        // Content Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.1f)),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStr,
                        color = com.mail2dev.planfora.ui.theme.SageGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color(0xFF1E2120))) {
                            DropdownMenuItem(text = { Text("Edit Note", fontSize = 12.sp) }, onClick = { showMenu = false; onEdit() })
                            DropdownMenuItem(text = { Text("Delete", color = Color.Red, fontSize = 12.sp) }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }

                Text(
                    text = log.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (log.note.isNotBlank()) {
                    Text(
                        text = log.note,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (log.imageUris.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(log.imageUris.split(",")) { path ->
                            AsyncImage(
                                model = File(path),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProductionDashboard(supply: DiySupplyEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("PRODUCTION DASHBOARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vessel / Jar ID", color = Color.Gray, fontSize = 10.sp)
                    Text(supply.containerId.ifBlank { "Unassigned" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Current Volume", color = Color.Gray, fontSize = 10.sp)
                    Text("${supply.currentVolume} ${supply.unit}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (supply.materialList.isNotBlank()) {
                Text("MATERIAL LIST / RECIPE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        supply.materialList.split("|").filter { it.contains(":") }.forEach { item ->
                            val parts = item.split(":")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(parts[0], color = Color.LightGray, fontSize = 13.sp)
                                Text(parts[1], color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductHeaderCard(
    supply: DiySupplyEntity,
    customFieldValues: List<com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity> = emptyList(),
    customFieldDefinitions: List<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity> = emptyList()
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Science, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp).size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(supply.name, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(supply.category, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            ProductMetadataGrid(supply)

            if (customFieldValues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    customFieldValues.forEach { value ->
                        val def = customFieldDefinitions.find { it.id == value.fieldDefId }
                        if (def != null && value.value.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Adjust, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "${def.fieldName}: ", color = Color.Gray, fontSize = 13.sp)
                                Text(text = value.value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (supply.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("INSTRUCTIONS / NOTES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(supply.notes, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            }

            if (supply.imageUris.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("ATTACHED IMAGES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(supply.imageUris.split(",")) { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = null,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (supply.audioPath != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("VOICE NOTE / INSTRUCTIONS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                InlineAudioPlayer(supply.audioPath)
            }
        }
    }
}

@Composable
fun ProductMetadataGrid(supply: DiySupplyEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ProductMetadataItem(Icons.Default.Tag, "Active Ingredient", supply.activeIngredient ?: "N/A", Modifier.weight(1f))
            ProductMetadataItem(Icons.Default.HourglassEmpty, "PHI (Days)", supply.phiDays?.toString() ?: "N/A", Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ProductMetadataItem(Icons.Default.Inventory, "Current Stock", "${supply.stockQuantity ?: 0.0} ${supply.stockUnit ?: ""}", Modifier.weight(1f))
            ProductMetadataItem(Icons.Default.LocationOn, "Storage Location", supply.locationNote.ifBlank { "Unassigned" }, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            val formulationDisplay = if (supply.formulationCode != null) {
                "${supply.formulationCode} (${supply.formType})"
            } else {
                supply.formType
            }
            ProductMetadataItem(Icons.Default.Category, "Formulation", formulationDisplay, Modifier.weight(1f))
        }
    }
}

@Composable
fun ProductMetadataItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SafetyAlertBanner(phi: Int) {
    Surface(
        color = Color(0xFFFFB74D).copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("SAFETY WARNING: PHI ACTIVE", color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("This product requires a $phi-day harvest interval.", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}
