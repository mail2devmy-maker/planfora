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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import com.mail2dev.planfora.ui.assets.AssetCategory
import com.mail2dev.planfora.ui.logs.ActivityThread
import com.mail2dev.planfora.ui.logs.LogsViewModel
import com.mail2dev.planfora.ui.navigation.Screen
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import androidx.compose.foundation.BorderStroke
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Long,
    navController: NavController,
    logsViewModel: LogsViewModel,
    assetsViewModel: com.mail2dev.planfora.ui.assets.AssetsViewModel,
    addAssetViewModel: com.mail2dev.planfora.ui.assets.AddAssetViewModel,
    profileViewModel: com.mail2dev.planfora.ui.profile.ProfileViewModel
) {
    val assets by logsViewModel.assets.collectAsState()
    val allLogs by logsViewModel.allLogs.collectAsState()
    val supplies by logsViewModel.supplies.collectAsState()
    val use24HourFormat by profileViewModel.use24HourFormat.collectAsState()
    val showEditSheet by assetsViewModel.showAddBottomSheet.collectAsState()
    
    val customFieldValues by logsViewModel.getCustomFieldValues(plantId).collectAsState(emptyList())
    val customFieldDefinitions by logsViewModel.getCustomFieldDefinitions(com.mail2dev.planfora.data.local.entity.FieldTargetType.ASSET_CATEGORY, AssetCategory.fromDatabase(assets.find { a -> a.id == plantId }?.category).displayName).collectAsState(emptyList())

    val plant = assets.find { it.id == plantId }
    val plantLogs = allLogs.filter { it.assetId == plantId }.sortedByDescending { it.timestamp }

    var showPromoteDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<JournalLogEntity?>(null) }

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

    if (showDeleteConfirm && plant != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Asset?", color = Color.White) },
            text = { Text("Are you sure you want to delete '${plant.name}' and all its linked log history? This cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        assetsViewModel.deleteAsset(plant)
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
                title = { Text("Asset Passport", color = Color.White, style = MaterialTheme.typography.titleMedium) },
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
                        DropdownMenuItem(
                            text = { Text("Promote Category", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                showPromoteDialog = true 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                addAssetViewModel.loadAsset(plantId)
                                assetsViewModel.setShowAddBottomSheet(true)
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
        containerColor = DarkBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    navController.navigate(Screen.NewLog.createRoute(timestamp = System.currentTimeMillis(), assetId = plantId))
                },
                containerColor = ForestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Log Activity") }
            )
        }
    ) { paddingValues ->
        if (plant == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Asset not found", color = Color.White)
            }
        } else {
            val activePhiLog = plantLogs.find { log ->
                val phiExpiry = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")?.toLongOrNull() ?: 0L
                System.currentTimeMillis() < phiExpiry
            }

            val zonesList = plant.zones.split(",").filter { it.isNotBlank() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    AssetPassportHeader(plant, customFieldValues, customFieldDefinitions)
                }

                if (zonesList.isNotEmpty()) {
                    item {
                        YieldComplianceCard(zonesList, plantLogs)
                    }
                }

                if (activePhiLog != null && zonesList.isEmpty()) {
                    item {
                        PhiAlertBanner(activePhiLog)
                    }
                }
                
                item {
                    Text(
                        text = "Linked Log History",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (plantLogs.isEmpty()) {
                    item {
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No logs found for this asset. Start tracking to see history here.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    val rootLogs = plantLogs.filter { it.parentLogId == null }
                    items(rootLogs) { rootLog ->
                        val followUps = plantLogs.filter { it.parentLogId == rootLog.id }.sortedBy { it.timestamp }
                        ActivityThread(
                            parentLog = rootLog,
                            followUps = followUps,
                            assetName = plant.name,
                            supplies = supplies,
                            use24Hour = use24HourFormat,
                            onFollowUpClick = {
                                navController.navigate(Screen.NewLog.createRoute(parentLogId = rootLog.id))
                            },
                            onDeleteLog = { logToDelete = it },
                            onEditLog = { log ->
                                navController.navigate(Screen.NewLog.createRoute(editingLogId = log.id))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditSheet) {
        com.mail2dev.planfora.ui.assets.AddAssetScreen(
            viewModel = addAssetViewModel,
            onDismiss = { assetsViewModel.setShowAddBottomSheet(false) }
        )
    }

    if (showPromoteDialog && plant != null) {
        AlertDialog(
            onDismissRequest = { showPromoteDialog = false },
            containerColor = Color(0xFF1E2120),
            title = { Text("Promote Category", color = Color.White) },
            text = {
                Column {
                    Text("Select new lifecycle stage:", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    AssetCategory.entries.filter { it != AssetCategory.ALL }.forEach { category ->
                        Surface(
                            onClick = {
                                assetsViewModel.promoteAssetCategory(plant, category)
                                showPromoteDialog = false
                            },
                            color = if (plant.category == category.displayName) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (category.iconRes != null) {
                                    Icon(
                                        painter = painterResource(id = category.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(category.icon, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(category.displayName, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPromoteDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun AssetPassportHeader(
    plant: PlantAssetEntity, 
    customFieldValues: List<com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity> = emptyList(),
    customFieldDefinitions: List<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity> = emptyList()
) {
    val category = AssetCategory.fromDatabase(plant.category)
    
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
                    if (category.iconRes != null) {
                        Icon(
                            painter = painterResource(id = category.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).padding(8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(category.icon, fontSize = 28.sp, modifier = Modifier.padding(8.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(plant.name, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${category.displayName} • 📍 ${plant.locationNote.ifBlank { "Unassigned" }}", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(16.dp))

            MetadataGrid(plant)

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

            if (plant.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("PERMANENT NOTES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(plant.notes, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            }

            if (plant.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    plant.tags.split(",").forEach { tag ->
                        Surface(
                            color = ForestGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("#$tag", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YieldComplianceCard(
    zones: List<String>,
    plantLogs: List<JournalLogEntity>
) {
    Surface(
        color = Color(0xFF1E2120),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Zone Compliance & Yield", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            zones.forEachIndexed { index, zone ->
                val logsForZone = plantLogs.filter { 
                    it.targetZones == null || it.targetZones!!.split(",").contains(zone) 
                }
                
                // PHI Calculation for this zone
                val activePhiLog = logsForZone.find { log ->
                    val phiExpiry = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")?.toLongOrNull() ?: 0L
                    System.currentTimeMillis() < phiExpiry
                }
                
                val phiExpiry = activePhiLog?.parameters?.split("|")?.find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")?.toLongOrNull() ?: 0L
                val remainingDays = if (phiExpiry > 0) ((phiExpiry - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).coerceAtLeast(1) else 0

                // Yield Calculation for this zone
                var totalYield = 0.0
                var yieldUnit = ""
                plantLogs.filter { it.activityType == "Harvest" }.forEach { log ->
                    val breakdown = log.parameters.split("|").find { it.startsWith("yield_breakdown:") }?.substringAfter("yield_breakdown:")
                    if (breakdown != null) {
                        val zoneYield = breakdown.split(";").find { it.startsWith("$zone=") }?.substringAfter("=")?.toDoubleOrNull() ?: 0.0
                        totalYield += zoneYield
                        yieldUnit = log.parameters.split("|").find { it.startsWith("unit:") }?.substringAfter("unit:") ?: ""
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(zone, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (totalYield > 0) {
                            Text("Cumulative: $totalYield $yieldUnit", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        }
                    }
                    
                    if (remainingDays > 0) {
                        Surface(
                            color = Color(0xFFFFB74D).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Locked: $remainingDays d",
                                color = Color(0xFFFFB74D),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                            color = ForestGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Safe",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (index < zones.size - 1) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}

@Composable
fun MetadataGrid(plant: PlantAssetEntity) {
    val category = AssetCategory.fromDatabase(plant.category)
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (category) {
            AssetCategory.SEEDLING -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetadataItem(Icons.Default.Science, "Batch / Tray ID", plant.tags.split(",").find { it.startsWith("Batch:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                    MetadataItem(Icons.Default.Inventory, "Quantity", plant.tags.split(",").find { it.startsWith("Qty:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                }
            }
            AssetCategory.CUTTING -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetadataItem(Icons.Default.Link, "Mother Plant Link", plant.tags.split(",").find { it.startsWith("Mother:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                    MetadataItem(Icons.Default.CalendarToday, "Propagated", plant.tags.split(",").find { it.startsWith("PropDate:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                }
            }
            AssetCategory.TREE -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetadataItem(Icons.Default.Fingerprint, "Physical ID", plant.tags.split(",").find { it.startsWith("PhysID:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                    MetadataItem(Icons.Default.LocationOn, "GPS", plant.tags.split(",").find { it.startsWith("GPS:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetadataItem(Icons.Default.Nature, "Rootstock", plant.tags.split(",").find { it.startsWith("Rootstock:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                    val plantedDateStr = plant.plantedDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) } ?: "Unassigned"
                    MetadataItem(Icons.Default.CalendarToday, "Planted Date", plantedDateStr, Modifier.weight(1f))
                }
            }
            AssetCategory.CROP -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetadataItem(Icons.Default.GridOn, "Plot / Row ID", plant.tags.split(",").find { it.startsWith("Plot:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                    MetadataItem(Icons.Default.Event, "Exp. Harvest", plant.tags.split(",").find { it.startsWith("ExpHarv:") }?.substringAfter(":") ?: "N/A", Modifier.weight(1f))
                }
            }
            else -> {}
        }
    }
}

@Composable
fun MetadataItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
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
fun PhiAlertBanner(log: JournalLogEntity) {
    val phiExpiryStr = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")
    val phiExpiry = phiExpiryStr?.toLongOrNull() ?: 0L
    val remainingDays = ((phiExpiry - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).coerceAtLeast(1)

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
                Text("ACTIVE PHI LOCKDOWN", color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("$remainingDays days remaining until harvest is safe.", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}
