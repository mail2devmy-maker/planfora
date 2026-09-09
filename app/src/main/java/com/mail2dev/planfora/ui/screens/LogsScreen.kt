package com.mail2dev.planfora.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.ui.components.InlineAudioPlayer
import com.mail2dev.planfora.ui.logs.CalendarMode
import com.mail2dev.planfora.ui.logs.LayoutMode
import com.mail2dev.planfora.ui.logs.LogsViewModel
import com.mail2dev.planfora.ui.navigation.Screen
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var logToDelete by remember { mutableStateOf<JournalLogEntity?>(null) }
    var selectedLogForDetail by remember { mutableStateOf<JournalLogEntity?>(null) }

    val logsListState = rememberLazyListState()

    val nestedScrollConnection = remember(calendarMode) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Collapse on downward scroll
                if (available.y < -15f && calendarMode == CalendarMode.MONTH) {
                    viewModel.setCalendarMode(CalendarMode.WEEK)
                }
                return Offset.Zero
            }
        }
    }

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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.NewLog.createRoute(timestamp = System.currentTimeMillis())) },
                containerColor = com.mail2dev.planfora.ui.theme.ForestGreen,
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
                .nestedScroll(nestedScrollConnection)
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
                calendarMode = calendarMode,
                onModeChange = viewModel::setCalendarMode
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (calendarMode == CalendarMode.DAY) {
                com.mail2dev.planfora.ui.logs.DayTimelineView(
                    logs = logs,
                    use24Hour = use24HourFormat,
                    onLogClick = { selectedLogForDetail = it },
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
                    state = logsListState,
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
                                    selectedLogForDetail = logs.find { it.id == id }
                                }
                            )
                        } else {
                            com.mail2dev.planfora.ui.logs.CompactLogItem(
                                log = rootLog, 
                                assetName = assetName, 
                                use24Hour = use24HourFormat,
                                onDeleteClick = { logToDelete = rootLog },
                                onEditClick = { navController.navigate(Screen.NewLog.createRoute(editingLogId = rootLog.id)) },
                                onClick = { selectedLogForDetail = rootLog }
                            )
                            followUps.forEach { childLog ->
                                com.mail2dev.planfora.ui.logs.CompactLogItem(
                                    log = childLog, 
                                    assetName = "↳ Follow-up", 
                                    use24Hour = use24HourFormat,
                                    onDeleteClick = { logToDelete = childLog },
                                    onEditClick = { navController.navigate(Screen.NewLog.createRoute(editingLogId = childLog.id)) },
                                    onClick = { selectedLogForDetail = childLog }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (selectedLogForDetail != null) {
        val assetName = assets.find { it.id == selectedLogForDetail!!.assetId }?.name ?: "General Log"
        LogDetailSheet(
            log = selectedLogForDetail!!,
            assetName = assetName,
            supplies = supplies,
            use24Hour = use24HourFormat,
            viewModel = viewModel,
            onDismiss = { selectedLogForDetail = null },
            onEdit = {
                val id = selectedLogForDetail!!.id
                selectedLogForDetail = null
                navController.navigate(Screen.NewLog.createRoute(editingLogId = id))
            },
            onDelete = {
                val log = selectedLogForDetail!!
                selectedLogForDetail = null
                logToDelete = log
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogDetailSheet(
    log: JournalLogEntity,
    assetName: String,
    supplies: List<com.mail2dev.planfora.data.local.entity.DiySupplyEntity>,
    use24Hour: Boolean,
    viewModel: LogsViewModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val customFieldValues by viewModel.getCustomFieldValues(log.id).collectAsState(emptyList())
    val customFieldDefinitions by viewModel.getCustomFieldDefinitions(com.mail2dev.planfora.data.local.entity.FieldTargetType.LOG_ACTIVITY, log.activityType).collectAsState(emptyList())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0B121C),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.activityType.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2120))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = Color.White) },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = com.mail2dev.planfora.util.TimeFormatter.formatDateTime(log.timestamp, use24Hour),
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = assetName, color = Color.Gray, fontSize = 13.sp)
            }

            com.mail2dev.planfora.ui.logs.PhiBadge(log)

            if (log.note.isNotBlank()) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.note,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (log.imageUris.isNotBlank()) {
                Text("PHOTOS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(log.imageUris.split(",")) { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = null,
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (log.audioFilePath != null) {
                Text("VOICE NOTE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                InlineAudioPlayer(log.audioFilePath)
            }

            if (log.parameters.isNotBlank() || log.tags.isNotBlank()) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                if (log.tags.isNotBlank()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        log.tags.split(",").forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "#$tag",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (log.parameters.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        log.parameters.split("|").forEach { param ->
                            val parts = param.split(":")
                            if (parts.size == 2 && parts[0] != "phi_expiry") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "${parts[0]}: ", color = Color.Gray, fontSize = 14.sp)
                                    Text(text = parts[1], color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (customFieldValues.isNotEmpty()) {
                    customFieldValues.forEach { value ->
                        val def = customFieldDefinitions.find { it.id == value.fieldDefId }
                        if (def != null && value.value.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${def.fieldName}: ", color = Color.Gray, fontSize = 14.sp)
                                Text(text = value.value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
            CalendarMode.entries.forEach { mode ->
                val isSelected = calendarMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onCalendarModeChange(mode) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                    tint = if (filtersActive) MaterialTheme.colorScheme.primary else Color.White
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
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLocation == null,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent
                )
            )
        }

        items(locations) { loc ->
            FilterChip(
                selected = selectedLocation == loc,
                onClick = { onLocationSelected(loc) },
                label = { Text(loc) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLocation == loc,
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}
