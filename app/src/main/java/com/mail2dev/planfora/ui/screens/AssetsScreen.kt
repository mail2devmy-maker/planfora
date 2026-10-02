package com.mail2dev.planfora.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import com.mail2dev.planfora.ui.assets.AddAssetScreen
import com.mail2dev.planfora.ui.assets.AddAssetViewModel
import com.mail2dev.planfora.ui.assets.AssetCategory
import com.mail2dev.planfora.ui.assets.AssetViewMode
import com.mail2dev.planfora.ui.assets.AssetsViewModel
import com.mail2dev.planfora.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AssetsScreen(
    navController: NavController,
    viewModel: AssetsViewModel,
    addAssetViewModel: AddAssetViewModel
) {
    val hierarchicalAssets by viewModel.hierarchicalAssets.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val availableLocations by viewModel.availableLocations.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddSheet by viewModel.showAddBottomSheet.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedAssetIds by viewModel.selectedAssetIds.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    val hasDraft by addAssetViewModel.hasDraftData.collectAsState()

    var showDraftConflictDialog by remember { mutableStateOf<PlantAssetEntity?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    var collapsedLocations by remember { mutableStateOf(setOf<String>()) }
    var collapsedBlocks by remember { mutableStateOf(setOf<String>()) }

    if (showDraftConflictDialog != null) {
        AlertDialog(
            onDismissRequest = { showDraftConflictDialog = null },
            title = { Text("Discard current draft?", color = Color.White) },
            text = { Text("You have an active draft for a new plant. Starting an edit will discard it. Continue?", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        addAssetViewModel.loadAsset(showDraftConflictDialog!!.id)
                        viewModel.setShowAddBottomSheet(true)
                        showDraftConflictDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Discard & Edit") }
            },
            dismissButton = {
                TextButton(onClick = { showDraftConflictDialog = null }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Color(0xFF1E2120)
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            selectedCategory = selectedCategory,
            selectedLocation = selectedLocation,
            availableLocations = availableLocations,
            onApplyFilters = { category, location ->
                viewModel.setCategory(category)
                viewModel.setLocation(location)
            },
            onResetFilters = {
                viewModel.setCategory(AssetCategory.ALL)
                viewModel.setLocation(null)
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            placeholder = { Text("Search name, ID, tag...", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = Color.Gray)
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
                    } else {
                        Text(
                            text = "Asset Manager",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close Search", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search", tint = Color.White)
                        }

                        IconButton(onClick = { showFilterSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (selectedCategory != AssetCategory.ALL || selectedLocation != null) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (selectedCategory != AssetCategory.ALL || selectedLocation != null) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = if (viewMode == AssetViewMode.HIERARCHY_LIST) Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.ViewList,
                                contentDescription = if (viewMode == AssetViewMode.HIERARCHY_LIST) "Switch to Grid View" else "Switch to List View",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!isMultiSelectMode) {
                Column(horizontalAlignment = Alignment.End) {
                    if (hasDraft && !showAddSheet) {
                        SmallFloatingActionButton(
                            onClick = { viewModel.setShowAddBottomSheet(true) },
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Spa,
                                contentDescription = "Resume Draft",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            if (hasDraft) {
                                viewModel.setShowAddBottomSheet(true)
                            } else {
                                addAssetViewModel.startNewAsset()
                                viewModel.setShowAddBottomSheet(true)
                            }
                        },
                        containerColor = com.mail2dev.planfora.ui.theme.ForestGreen,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "New Asset")
                    }
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
            // Active Filter Indicators Row
            if (selectedCategory != AssetCategory.ALL || selectedLocation != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedCategory != AssetCategory.ALL) {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setCategory(AssetCategory.ALL) },
                            label = { Text("Category: ${selectedCategory.displayName}", fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Clear Category",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    if (selectedLocation != null) {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.setLocation(null) },
                            label = { Text("Location: $selectedLocation", fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Clear Location",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            if (viewMode == AssetViewMode.HIERARCHY_LIST) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = if (isMultiSelectMode) 100.dp else 80.dp)
                ) {
                    if (hierarchicalAssets.isEmpty()) {
                        item {
                            EmptyAssetsView()
                        }
                    }

                    hierarchicalAssets.forEach { (location, blocks) ->
                        val isLocationCollapsed = collapsedLocations.contains(location)
                        val totalLocationPlants = blocks.values.flatten().sumOf { it.totalPlants }

                        item {
                            LocationHeader(
                                location = location,
                                totalPlants = totalLocationPlants,
                                isCollapsed = isLocationCollapsed,
                                onCollapseToggle = {
                                    collapsedLocations = if (isLocationCollapsed) collapsedLocations - location else collapsedLocations + location
                                }
                            )
                        }

                        if (!isLocationCollapsed) {
                            blocks.forEach { (blockName, assets) ->
                                val isUnassignedBlock = blockName.equals("UNASSIGNED ZONE", ignoreCase = true) ||
                                                        blockName.equals("UNASSIGNED", ignoreCase = true)

                                val blockKey = "$location-$blockName"
                                val isBlockCollapsed = collapsedBlocks.contains(blockKey)
                                val totalBlockPlants = assets.sumOf { it.totalPlants }

                                if (!isUnassignedBlock) {
                                    item {
                                        BlockHeader(
                                            blockName = blockName,
                                            assetCount = assets.size,
                                            totalPlants = totalBlockPlants,
                                            isCollapsed = isBlockCollapsed,
                                            onCollapseToggle = {
                                                collapsedBlocks = if (isBlockCollapsed) collapsedBlocks - blockKey else collapsedBlocks + blockKey
                                            },
                                            onSelectAll = { viewModel.selectAllInZone(assets) }
                                        )
                                    }
                                }

                                if (isUnassignedBlock || !isBlockCollapsed) {
                                    items(assets, key = { it.id }) { plantAsset ->
                                        Row(
                                            modifier = Modifier
                                                .padding(start = 12.dp)
                                                .height(IntrinsicSize.Min)
                                        ) {
                                            // Hierarchy Line
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .fillMaxHeight()
                                                    .background(com.mail2dev.planfora.ui.theme.SageGreen.copy(alpha = 0.3f))
                                            )

                                            val assetLogs = allLogs.filter { it.assetId == plantAsset.id }
                                            Box(modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)) {
                                                ListAssetCard(
                                                    asset = plantAsset,
                                                    logs = assetLogs,
                                                    isUnassignedBlock = isUnassignedBlock,
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
                        }
                    }
                }
            } else {
                // VISUAL_GRID
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = if (isMultiSelectMode) 100.dp else 80.dp)
                ) {
                    if (hierarchicalAssets.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptyAssetsView()
                        }
                    }

                    hierarchicalAssets.forEach { (location, blocks) ->
                        val isLocationCollapsed = collapsedLocations.contains(location)
                        val totalLocationPlants = blocks.values.flatten().sumOf { it.totalPlants }

                        item(span = { GridItemSpan(2) }) {
                            LocationHeader(
                                location = location,
                                totalPlants = totalLocationPlants,
                                isCollapsed = isLocationCollapsed,
                                onCollapseToggle = {
                                    collapsedLocations = if (isLocationCollapsed) collapsedLocations - location else collapsedLocations + location
                                }
                            )
                        }

                        if (!isLocationCollapsed) {
                            blocks.forEach { (blockName, assets) ->
                                val isUnassignedBlock = blockName.equals("UNASSIGNED ZONE", ignoreCase = true) ||
                                                        blockName.equals("UNASSIGNED", ignoreCase = true)

                                val blockKey = "$location-$blockName"
                                val isBlockCollapsed = collapsedBlocks.contains(blockKey)
                                val totalBlockPlants = assets.sumOf { it.totalPlants }

                                if (!isUnassignedBlock) {
                                    item(span = { GridItemSpan(2) }) {
                                        BlockHeader(
                                            blockName = blockName,
                                            assetCount = assets.size,
                                            totalPlants = totalBlockPlants,
                                            isCollapsed = isBlockCollapsed,
                                            onCollapseToggle = {
                                                collapsedBlocks = if (isBlockCollapsed) collapsedBlocks - blockKey else collapsedBlocks + blockKey
                                            },
                                            onSelectAll = { viewModel.selectAllInZone(assets) }
                                        )
                                    }
                                }

                                if (isUnassignedBlock || !isBlockCollapsed) {
                                    items(assets, key = { it.id }) { plantAsset ->
                                        val assetLogs = allLogs.filter { it.assetId == plantAsset.id }
                                        GridAssetCard(
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
fun EmptyAssetsView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.Forest,
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

@Composable
fun LocationHeader(
    location: String,
    totalPlants: Int,
    isCollapsed: Boolean,
    onCollapseToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCollapseToggle() }
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCollapsed) Icons.Rounded.ArrowRight else Icons.Rounded.ArrowDropDown,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = location.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )
        if (totalPlants > 0) {
            Text(
                text = "($totalPlants plants)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BlockHeader(
    blockName: String,
    assetCount: Int,
    totalPlants: Int,
    isCollapsed: Boolean,
    onCollapseToggle: () -> Unit,
    onSelectAll: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCollapseToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCollapsed) Icons.Rounded.ChevronRight else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = com.mail2dev.planfora.ui.theme.SageGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = blockName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (totalPlants > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "($totalPlants plants)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "$assetCount units inside",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            TextButton(
                onClick = { onSelectAll() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Select Block", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    selectedCategory: AssetCategory,
    selectedLocation: String?,
    availableLocations: List<String>,
    onApplyFilters: (AssetCategory, String?) -> Unit,
    onResetFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2120),
        contentColor = Color.White
    ) {
        var tempCategory by remember(selectedCategory) { mutableStateOf(selectedCategory) }
        var tempLocation by remember(selectedLocation) { mutableStateOf(selectedLocation) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Assets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Section
            Text(
                text = "CATEGORY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AssetCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = tempCategory == cat,
                        onClick = { tempCategory = cat },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (cat == AssetCategory.ALL) {
                                    Icon(
                                        Icons.Rounded.Folder,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                    )
                                } else if (cat.iconVector != null) {
                                    Icon(
                                        imageVector = cat.iconVector,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp).padding(end = 4.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else if (cat.icon.isNotBlank()) {
                                    Text(cat.icon, modifier = Modifier.padding(end = 4.dp))
                                }
                                Text(cat.displayName)
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
                            selected = tempCategory == cat,
                            borderColor = Color.Gray.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Location Section
            Text(
                text = "LOCATION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = tempLocation == null,
                    onClick = { tempLocation = null },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Place,
                                contentDescription = null,
                                tint = if (tempLocation == null) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                            )
                            Text("All Locations")
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
                        selected = tempLocation == null,
                        borderColor = Color.Gray.copy(alpha = 0.2f),
                        selectedBorderColor = Color.Transparent
                    )
                )

                availableLocations.forEach { loc ->
                    FilterChip(
                        selected = tempLocation == loc,
                        onClick = { tempLocation = loc },
                        label = { Text(loc) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = tempLocation == loc,
                            borderColor = Color.Gray.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        tempCategory = AssetCategory.ALL
                        tempLocation = null
                        onResetFilters()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        onApplyFilters(tempCategory, tempLocation)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListAssetCard(
    asset: PlantAssetEntity,
    logs: List<JournalLogEntity>,
    isUnassignedBlock: Boolean = false,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val category = AssetCategory.fromDatabase(asset.category)

    val activePhiLog = logs.find { log ->
        val phiExpiry = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")?.toLongOrNull() ?: 0L
        System.currentTimeMillis() < phiExpiry
    }

    val imageUri = asset.imageUris.split(",").firstOrNull { it.isNotBlank() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFF1E2120)
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
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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

            // 56dp x 56dp square rounded thumbnail
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (category.iconVector != null) {
                            Icon(
                                imageVector = category.iconVector,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else if (category.icon.isNotBlank()) {
                            Text(category.icon, fontSize = 22.sp)
                        } else {
                            Icon(
                                Icons.Rounded.Spa,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = asset.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    if (isUnassignedBlock || asset.subLocation.isBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Unassigned",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else if (asset.subLocation.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${asset.subLocation.uppercase()}]",
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tags chips
                val tagList = asset.tags.split(",").filter { it.isNotBlank() && !it.startsWith("PhysID:") && !it.startsWith("Batch:") }
                val physId = asset.tags.split(",").find { it.startsWith("PhysID:") }?.substringAfter(":") ?: ""

                if (physId.isNotBlank() || tagList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (physId.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ID: $physId",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        tagList.take(3).forEach { tag ->
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right-aligned column: PHI & Population counter
            Column(horizontalAlignment = Alignment.End) {
                if (asset.totalPlants > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${asset.totalPlants} plants",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (activePhiLog != null) {
                    val phiExpiryStr = activePhiLog.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")
                    val phiExpiry = phiExpiryStr?.toLongOrNull() ?: 0L
                    val remainingDays = ((phiExpiry - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).coerceAtLeast(1)

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFFFB74D).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Rounded.Warning, null, tint = Color(0xFFFFB74D), modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "PHI: ${remainingDays}d",
                                color = Color(0xFFFFB74D),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridAssetCard(
    asset: PlantAssetEntity,
    logs: List<JournalLogEntity>,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val category = AssetCategory.fromDatabase(asset.category)

    val activePhiLog = logs.find { log ->
        val phiExpiry = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")?.toLongOrNull() ?: 0L
        System.currentTimeMillis() < phiExpiry
    }

    val imageUri = asset.imageUris.split(",").firstOrNull { it.isNotBlank() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFF1E2120)
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
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.15f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            // Background / Image
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2A2E2C)),
                    contentAlignment = Alignment.Center
                ) {
                    if (category.iconVector != null) {
                        Icon(
                            imageVector = category.iconVector,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    } else if (category.icon.isNotBlank()) {
                        Text(category.icon, fontSize = 40.sp)
                    } else {
                        Icon(
                            Icons.Rounded.Spa,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Top Row: Category pill & Population badge & Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isMultiSelectMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = Color.White
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Category Pill
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            if (category.iconVector != null) {
                                Icon(
                                    imageVector = category.iconVector,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            } else if (category.icon.isNotBlank()) {
                                Text(category.icon, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = category.displayName,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Population badge
                if (asset.totalPlants > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${asset.totalPlants}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bottom Overlay: Plant Title, Sublocation, PHI badge
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = asset.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (asset.subLocation.isNotBlank()) {
                        Text(
                            text = asset.subLocation.uppercase(),
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (asset.locationNote.isNotBlank()) {
                        Text(
                            text = asset.locationNote,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Unassigned",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (activePhiLog != null) {
                        val phiExpiryStr = activePhiLog.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")
                        val phiExpiry = phiExpiryStr?.toLongOrNull() ?: 0L
                        val remainingDays = ((phiExpiry - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).coerceAtLeast(1)

                        Surface(
                            color = Color(0xFFFFB74D),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "PHI: ${remainingDays}d",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
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
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Batch", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
