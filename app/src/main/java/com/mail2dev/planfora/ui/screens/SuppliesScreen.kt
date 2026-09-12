package com.mail2dev.planfora.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.ui.navigation.Screen
import com.mail2dev.planfora.ui.supplies.AddSupplyScreen
import com.mail2dev.planfora.ui.supplies.AddSupplyViewModel
import com.mail2dev.planfora.ui.supplies.SuppliesViewModel
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SuppliesScreen(
    navController: androidx.navigation.NavController,
    viewModel: SuppliesViewModel,
    addSupplyViewModel: AddSupplyViewModel
) {
    val supplies by viewModel.supplies.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val showAddSheet by viewModel.showAddBottomSheet.collectAsState()
    
    val hasDraft by addSupplyViewModel.hasDraftData.collectAsState()
    val isUpdateDraft by addSupplyViewModel.isProductionUpdate.collectAsState()
    
    var showDraftConflictDialog by remember { mutableStateOf<DiySupplyEntity?>(null) }
    var showAddToolDialog by remember { mutableStateOf(false) }
    
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    if (showDraftConflictDialog != null) {
        AlertDialog(
            onDismissRequest = { showDraftConflictDialog = null },
            title = { Text("Discard current draft?", color = Color.White) },
            text = { Text("You have an active draft for a new product. Starting an update will discard it. Continue?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        addSupplyViewModel.loadSupply(showDraftConflictDialog!!.id, isProductionUpdate = true)
                        viewModel.setShowAddBottomSheet(true)
                        showDraftConflictDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Discard & Update") }
            },
            dismissButton = {
                TextButton(onClick = { showDraftConflictDialog = null }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Color(0xFF1E2120)
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            if (!showAddSheet) {
                Column(horizontalAlignment = Alignment.End) {
                    if (hasDraft) {
                        SmallFloatingActionButton(
                            onClick = { viewModel.setShowAddBottomSheet(true) },
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                if (isUpdateDraft) Icons.Rounded.PendingActions else Icons.Rounded.EditNote,
                                contentDescription = "Resume Draft",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    FloatingActionButton(
                        onClick = { 
                            if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.TOOLBOX) {
                                showAddToolDialog = true
                            } else if (hasDraft) {
                                // If draft exists, just reopen it
                                viewModel.setShowAddBottomSheet(true)
                            } else {
                                val isLab = selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.DIY_LAB
                                val defaultCat = if (isLab) 
                                    com.mail2dev.planfora.data.local.entity.SupplyCategory.DIY 
                                else 
                                    com.mail2dev.planfora.data.local.entity.SupplyCategory.INSECTICIDE
                                addSupplyViewModel.startNewSupply(defaultCat, isLab)
                                viewModel.setShowAddBottomSheet(true) 
                            }
                        },
                        containerColor = ForestGreen,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Item")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                // Tab Switcher
                Row(
                    modifier = Modifier
                        .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    com.mail2dev.planfora.ui.supplies.SupplyTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setTab(tab) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (tab) {
                                    com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY -> "Stock"
                                    com.mail2dev.planfora.ui.supplies.SupplyTab.DIY_LAB -> "DIY Lab"
                                    com.mail2dev.planfora.ui.supplies.SupplyTab.TOOLBOX -> "Toolbox"
                                },
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            val searchQuery by viewModel.searchQuery.collectAsState()
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    focusedContainerColor = Color(0xFF1E2120),
                    unfocusedContainerColor = Color(0xFF1E2120)
                )
            )

            if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) {
                SupplyCategoryFilters(
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::setCategory
                )
            }

            val measurementTools by viewModel.measurementTools.collectAsState()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.TOOLBOX) {
                    if (measurementTools.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Rounded.Construction,
                                title = "Toolbox is empty",
                                subtitle = "Tap + to register scoops, spoons or sprayers"
                            )
                        }
                    }
                    items(measurementTools) { tool ->
                        ToolCard(
                            tool = tool,
                            onDelete = { viewModel.deleteMeasurementTool(tool) }
                        )
                    }
                } else {
                    if (supplies.isEmpty()) {
                        item {
                            EmptyState(
                                icon = if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) Icons.Rounded.Inventory2 else Icons.Rounded.Science,
                                title = if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) "No supplies found" else "DIY Lab is empty",
                                subtitle = "Tap + to ${if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) "add inventory" else "start a batch"}"
                            )
                        }
                    }

                    items(supplies) { supply ->
                        val onClick = { navController.navigate(Screen.SupplyDetail.createRoute(supply.id)) }
                        if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.DIY_LAB) {
                            FormulationCard(
                                formulation = supply, 
                                onClick = onClick,
                                onLongClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    if (hasDraft && !isUpdateDraft) {
                                        showDraftConflictDialog = supply
                                    } else {
                                        addSupplyViewModel.loadSupply(supply.id, isProductionUpdate = true)
                                        viewModel.setShowAddBottomSheet(true)
                                    }
                                },
                                onFinalize = { yield -> viewModel.finalizeBatch(supply, yield) }
                            )
                        } else {
                            StoreSupplyCard(supply, onClick)
                        }
                    }
                }
            }
        }
    }

    if (showAddToolDialog) {
        AddToolDialog(
            onDismiss = { showAddToolDialog = false },
            onConfirm = { name, cap, unit ->
                viewModel.addMeasurementTool(name, cap, unit)
                showAddToolDialog = false
            }
        )
    }

    if (showAddSheet) {
        AddSupplyScreen(
            viewModel = addSupplyViewModel,
            onDismiss = { viewModel.setShowAddBottomSheet(false) }
        )
    }
}

@Composable
fun SupplyCategoryFilters(
    selectedCategory: com.mail2dev.planfora.ui.supplies.SupplyCategory,
    onCategorySelected: (com.mail2dev.planfora.ui.supplies.SupplyCategory) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(com.mail2dev.planfora.ui.supplies.SupplyCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    labelColor = Color.Gray,
                    containerColor = Color.Transparent
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == category,
                    borderColor = Color.Gray.copy(alpha = 0.5f),
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun StoreSupplyCard(supply: DiySupplyEntity, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = supply.name, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${supply.currentVolume} / ${supply.originalVolume}", 
                    color = Color.Gray, 
                    fontSize = 12.sp
                )
            }
            
            if (supply.originalVolume > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (supply.currentVolume / supply.originalVolume).toFloat().coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = com.mail2dev.planfora.ui.theme.ForestGreen,
                    trackColor = Color.DarkGray,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = Color.Gray.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ToolCard(
    tool: com.mail2dev.planfora.data.local.entity.MeasurementToolEntity,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(tool.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${tool.capacity} ${tool.unit}", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete Tool", tint = Color.Gray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AddToolDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("ml") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Tool", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tool Name (e.g., Blue Scoop)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(0.6f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val cap = capacity.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && cap > 0) {
                        onConfirm(name, cap, unit)
                    }
                },
                enabled = name.isNotBlank() && capacity.toDoubleOrNull() != null
            ) { Text("Save Tool") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) }
        },
        containerColor = Color(0xFF1E2120)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FormulationCard(
    formulation: DiySupplyEntity, 
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFinalize: (Double) -> Unit
) {
    val currentTime = System.currentTimeMillis()
    val isMature = currentTime >= formulation.targetMaturityDate
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(formulation.batchCode, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formulation.name, color = Color.Gray, fontSize = 12.sp)
                }
                StatusBadge(formulation, currentTime, isMature)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formulation.currentVolume} ${formulation.unit}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                
                Surface(
                    onClick = {
                        if (isMature) {
                            onFinalize(formulation.currentVolume)
                        } else {
                            onLongClick()
                        }
                    },
                    color = (if (isMature) ForestGreen else MaterialTheme.colorScheme.primary).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, (if (isMature) ForestGreen else MaterialTheme.colorScheme.primary).copy(alpha = 0.5f)),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        if (isMature) "Move to Stock" else "Update",
                        color = if (isMature) ForestGreen else MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(formulation: DiySupplyEntity, currentTime: Long, isMature: Boolean) {
    val (badgeColor, label) = if (isMature) {
        val ageDays = TimeUnit.MILLISECONDS.toDays(currentTime - formulation.targetMaturityDate)
        com.mail2dev.planfora.ui.theme.ForestGreen to "Mature / Ready ($ageDays days old)"
    } else {
        val remainingDays = TimeUnit.MILLISECONDS.toDays(formulation.targetMaturityDate - currentTime)
        Color(0xFFFFB74D) to "Maturing ($remainingDays days left)"
    }

    Surface(
        color = badgeColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor)
    ) {
        Text(
            text = label,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
