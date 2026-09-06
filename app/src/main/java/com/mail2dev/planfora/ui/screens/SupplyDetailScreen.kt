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
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.ui.logs.LogsViewModel
import com.mail2dev.planfora.ui.navigation.Screen
import com.mail2dev.planfora.ui.supplies.SuppliesViewModel
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
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
    
    val allLogs by logsViewModel.allLogs.collectAsState()
    val supplyLogs = allLogs.filter { it.supplyId == supplyId }.sortedByDescending { it.timestamp }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<com.mail2dev.planfora.data.local.entity.JournalLogEntity?>(null) }

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
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = SageGreen)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2120))
                    ) {
                        DropdownMenuItem(
                            text = { Text("✏️ Edit Supply", color = Color.White) },
                            onClick = { 
                                showMenu = false
                                addSupplyViewModel.loadSupply(supplyId)
                                viewModel.setShowAddBottomSheet(true)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🗑️ Delete Supply", color = Color.Red) },
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
                    ProductHeaderCard(supply)
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
                        color = SageGreen,
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
                                "No applications logged yet for this product.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(supplyLogs) { log ->
                        com.mail2dev.planfora.ui.logs.CompactLogItem(
                            log = log,
                            assetName = logsViewModel.assets.collectAsState().value.find { it.id == log.assetId }?.name ?: "General",
                            use24Hour = false,
                            onDeleteClick = { logToDelete = log },
                            onEditClick = { navController.navigate(Screen.NewLog.createRoute(editingLogId = log.id)) }
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
fun ProductHeaderCard(supply: DiySupplyEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SageGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Science, null, tint = SageGreen, modifier = Modifier.padding(10.dp).size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(supply.name, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(supply.category, color = SageGreen, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            ProductMetadataGrid(supply)

            if (supply.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("INSTRUCTIONS / NOTES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(supply.notes, color = Color.LightGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
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
