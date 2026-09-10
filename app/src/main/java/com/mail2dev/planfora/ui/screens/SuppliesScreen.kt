package com.mail2dev.planfora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            if (!showAddSheet) {
                FloatingActionButton(
                    onClick = { 
                        addSupplyViewModel.startNewSupply()
                        viewModel.setShowAddBottomSheet(true) 
                    },
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Formulation")
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
                                text = if (tab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) "Stock" else "DIY Lab",
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                if (supplies.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) Icons.Rounded.Inventory2 else Icons.Rounded.Science,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) "No supplies found" else "DIY Lab is empty",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Tap + to ${if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.INVENTORY) "add inventory" else "start a batch"}",
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                items(supplies) { supply ->
                    val onClick = { navController.navigate(Screen.SupplyDetail.createRoute(supply.id)) }
                    if (selectedTab == com.mail2dev.planfora.ui.supplies.SupplyTab.DIY_LAB) {
                        FormulationCard(supply, onClick)
                    } else {
                        StoreSupplyCard(supply, onClick)
                    }
                }
            }
        }
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
fun FormulationCard(formulation: DiySupplyEntity, onClick: () -> Unit) {
    val currentTime = System.currentTimeMillis()
    val isMature = currentTime >= formulation.targetMaturityDate
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
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
            
            Text(
                text = "${formulation.currentVolume} ${formulation.unit}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
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
