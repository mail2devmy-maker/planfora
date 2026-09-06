package com.mail2dev.planfora.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mail2dev.planfora.ui.logs.CalendarMode
import com.mail2dev.planfora.ui.logs.LayoutMode
import com.mail2dev.planfora.ui.logs.LogsViewModel
import com.mail2dev.planfora.ui.navigation.Screen
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    navController: NavController, 
    viewModel: LogsViewModel, 
    profileViewModel: com.mail2dev.planfora.ui.profile.ProfileViewModel
) {
    val logs by viewModel.filteredLogs.collectAsState()
    val eventDates by viewModel.logEventDates.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val supplies by viewModel.supplies.collectAsState()
    val calendarMode by viewModel.calendarMode.collectAsState()
    val layoutMode by viewModel.layoutMode.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val locationFilter by viewModel.locationFilter.collectAsState()
    val phiFilterActive by viewModel.phiFilterActive.collectAsState()
    val masterLocations by viewModel.masterLocations.collectAsState()
    val use24HourFormat by profileViewModel.use24HourFormat.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<com.mail2dev.planfora.data.local.entity.JournalLogEntity?>(null) }

    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Delete Log Entry?", color = Color.White) },
            text = { Text("Are you sure you want to remove this log? This cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLog(logToDelete!!)
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

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.NewLog.createRoute(timestamp = System.currentTimeMillis())) },
                containerColor = ForestGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Log")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LogsHeader(
                calendarMode = calendarMode,
                layoutMode = layoutMode,
                onCalendarModeChange = viewModel::setCalendarMode,
                onLayoutModeChange = {
                    val newMode = if (layoutMode == LayoutMode.EXPANDED_CARD) LayoutMode.COMPACT_LIST else LayoutMode.EXPANDED_CARD
                    viewModel.setLayoutMode(newMode)
                },
                onToggleFilters = { showFilters = !showFilters },
                filtersActive = locationFilter != null || phiFilterActive
            )

            AnimatedVisibility(visible = showFilters) {
                FilterStrip(
                    locations = masterLocations,
                    selectedLocation = locationFilter,
                    phiActive = phiFilterActive,
                    onLocationSelected = viewModel::setLocationFilter,
                    onTogglePhi = viewModel::togglePhiFilter
                )
            }

            com.mail2dev.planfora.ui.logs.CalendarDateStrip(
                selectedDate = selectedDate,
                onDateSelected = viewModel::setSelectedDate,
                onDateLongClick = { timestamp ->
                    navController.navigate(Screen.NewLog.createRoute(timestamp = timestamp))
                },
                eventDates = eventDates,
                calendarMode = calendarMode
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (calendarMode == CalendarMode.DAY) {
                com.mail2dev.planfora.ui.logs.DayTimelineView(
                    logs = logs,
                    use24Hour = use24HourFormat,
                    onLogClick = { /* Maybe navigate to detail or edit */ },
                    onHourLongClick = { hour ->
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, hour)
                        }
                        navController.navigate(Screen.NewLog.createRoute(timestamp = cal.timeInMillis))
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rootLogs = logs.filter { it.parentLogId == null }
                    
                    if (rootLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.EventNote,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "No logs recorded for this day",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    items(rootLogs) { rootLog ->
                        val assetName = assets.find { it.id == rootLog.assetId }?.name ?: "General Log"
                        val followUps = logs.filter { it.parentLogId == rootLog.id }.sortedBy { it.timestamp }
                        
                        if (layoutMode == LayoutMode.EXPANDED_CARD) {
                            com.mail2dev.planfora.ui.logs.ActivityThread(
                                parentLog = rootLog,
                                followUps = followUps,
                                assetName = assetName,
                                supplies = supplies,
                                use24Hour = use24HourFormat,
                                onFollowUpClick = {
                                    navController.navigate(Screen.NewLog.createRoute(parentLogId = rootLog.id))
                                },
                                onDeleteLog = { logToDelete = it },
                                onEditLog = { log ->
                                    navController.navigate(Screen.NewLog.createRoute(editingLogId = log.id))
                                },
                                onLogClick = { id ->
                                    navController.navigate(Screen.NewLog.createRoute(editingLogId = id))
                                }
                            )
                        } else {
                            com.mail2dev.planfora.ui.logs.CompactLogItem(
                                log = rootLog, 
                                assetName = assetName, 
                                use24Hour = use24HourFormat,
                                onDeleteClick = { logToDelete = rootLog },
                                onEditClick = { navController.navigate(Screen.NewLog.createRoute(editingLogId = rootLog.id)) },
                                onClick = { navController.navigate(Screen.NewLog.createRoute(editingLogId = rootLog.id)) }
                            )
                            followUps.forEach { childLog ->
                                com.mail2dev.planfora.ui.logs.CompactLogItem(
                                    log = childLog, 
                                    assetName = "↳ Follow-up", 
                                    use24Hour = use24HourFormat,
                                    onDeleteClick = { logToDelete = childLog },
                                    onEditClick = { navController.navigate(Screen.NewLog.createRoute(editingLogId = childLog.id)) },
                                    onClick = { navController.navigate(Screen.NewLog.createRoute(editingLogId = childLog.id)) }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun LogsHeader(
    calendarMode: CalendarMode,
    layoutMode: LayoutMode,
    onCalendarModeChange: (CalendarMode) -> Unit,
    onLayoutModeChange: () -> Unit,
    onToggleFilters: () -> Unit,
    filtersActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            CalendarMode.values().forEach { mode ->
                val isSelected = calendarMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) SageGreen else Color.Transparent)
                        .clickable { onCalendarModeChange(mode) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        color = if (isSelected) DarkBackground else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleFilters) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = if (filtersActive) SageGreen else Color.White
                )
            }
            IconButton(onClick = onLayoutModeChange) {
                Icon(
                    imageVector = if (layoutMode == LayoutMode.EXPANDED_CARD) Icons.Default.List else Icons.Default.Menu,
                    contentDescription = "Toggle Layout",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun FilterStrip(
    locations: List<String>,
    selectedLocation: String?,
    phiActive: Boolean,
    onLocationSelected: (String?) -> Unit,
    onTogglePhi: () -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChip(
                selected = phiActive,
                onClick = onTogglePhi,
                label = { Text("⚠️ Active PHI") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFB74D),
                    selectedLabelColor = Color.Black
                ),
                leadingIcon = { if (phiActive) Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
            )
        }

        item {
            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
        }

        item {
            FilterChip(
                selected = selectedLocation == null,
                onClick = { onLocationSelected(null) },
                label = { Text("All Zones") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SageGreen, selectedLabelColor = DarkBackground)
            )
        }

        items(locations) { loc ->
            FilterChip(
                selected = selectedLocation == loc,
                onClick = { onLocationSelected(loc) },
                label = { Text(loc) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SageGreen, selectedLabelColor = DarkBackground)
            )
        }
    }
}
