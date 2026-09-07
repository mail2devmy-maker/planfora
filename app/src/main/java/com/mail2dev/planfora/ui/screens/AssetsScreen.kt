package com.mail2dev.planfora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import com.mail2dev.planfora.ui.assets.AddAssetScreen
import com.mail2dev.planfora.ui.assets.AddAssetViewModel
import com.mail2dev.planfora.ui.assets.AssetCategory
import com.mail2dev.planfora.ui.assets.AssetsViewModel
import com.mail2dev.planfora.ui.navigation.Screen
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssetsScreen(
    navController: NavController,
    viewModel: AssetsViewModel,
    addAssetViewModel: AddAssetViewModel
) {
    val groupedAssets by viewModel.groupedAssets.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddSheet by viewModel.showAddBottomSheet.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedAssetIds by viewModel.selectedAssetIds.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!isMultiSelectMode) {
                FloatingActionButton(
                    onClick = { 
                        addAssetViewModel.startNewAsset()
                        viewModel.setShowAddBottomSheet(true) 
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Asset")
                }
            }
        },
        bottomBar = {
            if (isMultiSelectMode) {
                BatchSelectionBar(
                    selectedCount = selectedAssetIds.size,
                    onLogBatch = {
                        val idsString = selectedAssetIds.joinToString(",")
                        navController.navigate(Screen.NewLog.createRoute(assetId = null) + "&assetIds=$idsString")
                        viewModel.clearSelection()
                    },
                    onCancel = { viewModel.clearSelection() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Asset Manager",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search by name, ID, or tag...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            CategoryFilters(
                selectedCategory = selectedCategory,
                onCategorySelected = viewModel::setCategory
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = if (isMultiSelectMode) 100.dp else 80.dp)
            ) {
                if (groupedAssets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No assets found",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Tap + to add your first plant",
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                groupedAssets.forEach { (location, assets) ->
                    item {
                        LocationHeader(
                            location = location,
                            onSelectAll = { viewModel.selectAllInZone(assets) }
                        )
                    }
                    items(assets) { plantAsset ->
                        val assetLogs = allLogs.filter { it.assetId == plantAsset.id }
                        AssetCard(
                            asset = plantAsset,
                            logs = assetLogs,
                            isSelected = selectedAssetIds.contains(plantAsset.id),
                            isMultiSelectMode = isMultiSelectMode,
                            onLongClick = { viewModel.toggleAssetSelection(plantAsset.id) },
                            onClick = {
                                if (isMultiSelectMode) {
                                    viewModel.toggleAssetSelection(plantAsset.id)
                                } else {
                                    navController.navigate(Screen.PlantDetail.createRoute(plantAsset.id))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddAssetScreen(
            viewModel = addAssetViewModel,
            onDismiss = { viewModel.setShowAddBottomSheet(false) }
        )
    }
}

@Composable
fun LocationHeader(location: String, onSelectAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = location,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onSelectAll) {
            Text("Select Zone", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
fun CategoryFilters(
    selectedCategory: AssetCategory,
    onCategorySelected: (AssetCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AssetCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (category.icon.isNotBlank()) {
                            Text(category.icon, modifier = Modifier.padding(end = 4.dp))
                        }
                        Text(category.displayName) 
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == category,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssetCard(
    asset: com.mail2dev.planfora.data.local.entity.PlantAssetEntity,
    logs: List<JournalLogEntity>, 
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val category = AssetCategory.entries.find { it.displayName == asset.category } ?: AssetCategory.TREE
    
    val activePhiLog = logs.find { log ->
        val phiExpiry = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")?.toLongOrNull() ?: 0L
        System.currentTimeMillis() < phiExpiry
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMultiSelectMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(category.icon, fontSize = 20.sp, modifier = Modifier.padding(6.dp))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(asset.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val physId = asset.tags.split(",").find { it.startsWith("PhysID:") }?.substringAfter(":") ?: ""
                    if (physId.isNotBlank()) {
                        Text("ID: $physId", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (activePhiLog != null) {
                    val phiExpiryStr = activePhiLog.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")
                    val phiExpiry = phiExpiryStr?.toLongOrNull() ?: 0L
                    val remainingDays = ((phiExpiry - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).coerceAtLeast(1)
                    
                    Surface(
                        color = Color(0xFFFFB74D).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "⚠️ PHI: $remainingDays d",
                            color = Color(0xFFFFB74D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (asset.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    asset.tags.split(",").filter { !it.startsWith("PhysID:") && !it.startsWith("Batch:") }.take(4).forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatchSelectionBar(
    selectedCount: Int,
    onLogBatch: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        color = Color(0xFF1E2120),
        tonalElevation = 8.dp,
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
                Text(
                    text = "$selectedCount Assets Selected",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Ready for batch logging",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = Color.Gray)
                }
                Button(
                    onClick = onLogBatch,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Batch", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
