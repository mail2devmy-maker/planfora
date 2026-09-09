package com.mail2dev.planfora.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import com.mail2dev.planfora.ui.assets.AssetCategory
import com.mail2dev.planfora.ui.components.CreateCustomFieldDialog
import com.mail2dev.planfora.ui.components.DynamicCustomFieldInput
import com.mail2dev.planfora.ui.components.ManageFieldDialog
import com.mail2dev.planfora.ui.components.HarvestActivityCard
import com.mail2dev.planfora.ui.components.MediaAttachmentStrip
import com.mail2dev.planfora.ui.components.PlanForaFieldGroup
import com.mail2dev.planfora.ui.components.PlanForaSurfaceCard
import com.mail2dev.planfora.ui.components.TagPickerSheet
import com.mail2dev.planfora.ui.components.planForaTextFieldColors
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import com.mail2dev.planfora.ui.logs.LogsViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun NewLogEntryScreen(
    navController: NavController,
    viewModel: LogsViewModel,
    parentLogId: Long? = null,
    initialTimestamp: Long? = null,
    initialAssetId: Long? = null,
    initialAssetIds: String? = null,
    editingLogId: Long? = null
) {
    val assets by viewModel.assets.collectAsState()
    val supplies by viewModel.supplies.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val masterTags by viewModel.masterTags.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var editingLog by remember { mutableStateOf<JournalLogEntity?>(null) }

    val recentlyUsedSupplies = remember(allLogs, supplies) {
        allLogs.filter { it.supplyId != null }
            .groupBy { it.supplyId!! }
            .toList()
            .sortedByDescending { it.second.size }
            .take(3)
            .mapNotNull { (id, _) -> supplies.find { it.id == id } }
    }
    
    var selectedAssetIds by remember { 
        mutableStateOf(
            initialAssetIds?.split(",")?.mapNotNull { it.toLongOrNull() }?.toSet() 
            ?: initialAssetId?.let { setOf(it) } 
            ?: emptySet()
        ) 
    }
    var selectedSupplyId by remember { mutableStateOf<Long?>(null) }
    var customInputName by remember { mutableStateOf("") }
    
    var title by remember { mutableStateOf("") }
    var userEditedTitle by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("Observation") }
    var customActivity by remember { mutableStateOf("") }
    var showCustomActivityInput by remember { mutableStateOf(false) }
    
    var tags by remember { mutableStateOf(setOf<String>()) }
    var parameters by remember { mutableStateOf(mutableMapOf<String, String>()) }

    // Dynamic Custom Fields State
    val customFieldDefinitions by viewModel.getCustomFieldDefinitions(FieldTargetType.LOG_ACTIVITY, activityType).collectAsState(emptyList())
    var customFieldValues by remember { mutableStateOf(mutableMapOf<Long, String>()) }
    var showCustomFieldDialog by remember { mutableStateOf(false) }
    var fieldToManage by remember { mutableStateOf<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity?>(null) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    
    var imageUris by remember { mutableStateOf(listOf<Uri>()) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    
    // Activity-Specific State
    var dosageAmount by remember { mutableStateOf("") }
    var dosageRatio by remember { mutableStateOf("mL / Liter") }
    var appMethod by remember { mutableStateOf("FOLIAR SPRAY") }
    
    var yieldAmount by remember { mutableStateOf("") }
    var yieldUnit by remember { mutableStateOf("kg") }
    var qualityGrade by remember { mutableStateOf("A") }
    
    var substrateMix by remember { mutableStateOf("") }
    var potSize by remember { mutableStateOf("") }
    
    var pruningType by remember { mutableStateOf("Sanitary") }
    
    var selectedZones by remember { mutableStateOf(setOf<String>()) }
    var rowYields by remember { mutableStateOf(mutableMapOf<String, String>()) }

    var parentLog by remember { mutableStateOf<JournalLogEntity?>(null) }
    var selectedTimestamp by remember { mutableStateOf(initialTimestamp ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(userEditedTitle, note, tags, imageUris, audioPath, customActivity, dosageAmount, yieldAmount, substrateMix) {
        userEditedTitle || note.isNotBlank() || tags.isNotEmpty() || imageUris.isNotEmpty() || audioPath != null || 
        customActivity.isNotBlank() || dosageAmount.isNotBlank() || yieldAmount.isNotBlank() || substrateMix.isNotBlank()
    }

    BackHandler(enabled = hasUnsavedChanges && editingLogId == null) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?", color = Color.White) },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { 
                    showDiscardDialog = false
                    navController.popBackStack() 
                }) {
                    Text("Discard", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Continue Editing", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E2120)
        )
    }

    val activityTypes = listOf("Observation", "Feeding", "Pruning", "Pest Control", "Repotting", "Harvest", "Other")

    // Loading editing log
    LaunchedEffect(editingLogId) {
        editingLogId?.let { id ->
            viewModel.getLogById(id)?.let { log ->
                editingLog = log
                title = log.title
                userEditedTitle = true
                note = log.note
                activityType = if (activityTypes.contains(log.activityType)) log.activityType else "Other"
                if (activityType == "Other") customActivity = log.activityType
                selectedAssetIds = setOf(log.assetId)
                selectedSupplyId = log.supplyId
                customInputName = log.customInputName ?: ""
                tags = log.tags.split(",").filter { it.isNotBlank() }.toSet()
                
                // Parse parameters
                val params = log.parameters.split("|").associate { 
                    val parts = it.split(":")
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }.filterKeys { it.isNotBlank() }
                
                parameters = params.toMutableMap()
                
                dosageAmount = params["dosage"] ?: ""
                dosageRatio = params["ratio"] ?: "mL / Liter"
                appMethod = params["method"] ?: "FOLIAR SPRAY"
                
                substrateMix = params["substrate"] ?: ""
                potSize = params["pot_size"] ?: ""
                
                pruningType = params["pruning_type"] ?: "Sanitary"
                
                yieldUnit = params["unit"] ?: "kg"
                qualityGrade = params["grade"] ?: "A"
                
                if (params.containsKey("yield_breakdown")) {
                    val breakdown = params["yield_breakdown"]!!
                    rowYields = breakdown.split(";").associate { 
                        val parts = it.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else "" to ""
                    }.toMutableMap()
                    selectedZones = rowYields.keys.filter { it.isNotBlank() }.toSet()
                } else {
                    rowYields["Total"] = params["yield"] ?: ""
                }
                
                if (log.targetZones != null) {
                    selectedZones = log.targetZones.split(",").toSet()
                }
            }
        }
    }

    val availableZones = remember(selectedAssetIds, assets) {
        if (selectedAssetIds.size == 1) {
            assets.find { it.id == selectedAssetIds.first() }?.zones?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    var showAssetPicker by remember { mutableStateOf(false) }
    var showSupplyBottomSheet by remember { mutableStateOf(false) }
    var showTagSheet by remember { mutableStateOf(false) }
    var customInputCategory by remember { mutableStateOf("Other") }

    // Auto-Generated Title Logic
    LaunchedEffect(selectedAssetIds, activityType, selectedSupplyId, customInputName, yieldAmount, yieldUnit, substrateMix, pruningType, dosageAmount, dosageRatio) {
        if (!userEditedTitle) {
            val assetName = if (selectedAssetIds.isEmpty()) {
                "General Log"
            } else if (selectedAssetIds.size == 1) {
                assets.find { it.id == selectedAssetIds.first() }?.name ?: "Unknown Asset"
            } else {
                "${selectedAssetIds.size} Assets"
            }

            val keyParam = when (activityType) {
                "Pest Control", "Feeding" -> {
                    val supply = supplies.find { it.id == selectedSupplyId }?.batchCode ?: customInputName
                    if (dosageAmount.isNotBlank()) "$supply ($dosageAmount $dosageRatio)" else supply
                }
                "Harvest" -> if (yieldAmount.isNotBlank()) "$yieldAmount $yieldUnit" else ""
                "Repotting" -> substrateMix
                "Pruning" -> pruningType
                else -> ""
            }

            title = if (keyParam.isNotBlank()) "$assetName • $activityType • $keyParam" else "$assetName • $activityType"
        }
    }

    // Contextual Dosage Memory Effect
    LaunchedEffect(selectedSupplyId, appMethod) {
        if ((activityType == "Pest Control" || activityType == "Feeding") && selectedSupplyId != null) {
            val supplyLogs = viewModel.getLogsBySupply(selectedSupplyId!!)
            val matchingLog = supplyLogs.firstOrNull { log ->
                val params = log.parameters.split("|").associate { 
                    val parts = it.split(":")
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }
                params["method"] == appMethod
            }
            
            matchingLog?.let { log ->
                val params = log.parameters.split("|").associate { 
                    val parts = it.split(":")
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }
                dosageAmount = params["dosage"] ?: dosageAmount
                dosageRatio = params["ratio"] ?: dosageRatio
            }
        }
    }

    LaunchedEffect(parentLogId) {
        parentLogId?.let {
            parentLog = viewModel.getLogById(it)
            parentLog?.let { parent ->
                selectedAssetIds = setOf(parent.assetId)
                title = "Follow-up: ${parent.title}"
                userEditedTitle = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = title,
                            onValueChange = { 
                                title = it 
                                userEditedTitle = true
                            },
                            placeholder = { Text("Log Title...", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (hasUnsavedChanges && editingLogId == null) {
                            showDiscardDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (showDatePicker) {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            DisposableEffect(Unit) {
                val datePicker = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        cal.set(java.util.Calendar.YEAR, year)
                        cal.set(java.util.Calendar.MONTH, month)
                        cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                        selectedTimestamp = cal.timeInMillis
                        showDatePicker = false
                        showTimePicker = true // Chain to time picker
                    },
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                )
                datePicker.setOnCancelListener { showDatePicker = false }
                datePicker.show()
                onDispose { datePicker.dismiss() }
            }
        }

        if (showTimePicker) {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            DisposableEffect(Unit) {
                val timePicker = android.app.TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        cal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(java.util.Calendar.MINUTE, minute)
                        selectedTimestamp = cal.timeInMillis
                        showTimePicker = false
                    },
                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                    cal.get(java.util.Calendar.MINUTE),
                    false
                )
                timePicker.setOnCancelListener { showTimePicker = false }
                timePicker.show()
                onDispose { timePicker.dismiss() }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val dateText = remember(selectedTimestamp) {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(selectedTimestamp))
            }
            val timeText = remember(selectedTimestamp) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(selectedTimestamp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { showDatePicker = true },
                    label = { Text(dateText, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ),
                    border = AssistChipDefaults.assistChipBorder(borderColor = Color.Gray.copy(alpha = 0.3f), enabled = true)
                )
                AssistChip(
                    onClick = { showTimePicker = true },
                    label = { Text(timeText, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ),
                    border = AssistChipDefaults.assistChipBorder(borderColor = Color.Gray.copy(alpha = 0.3f), enabled = true)
                )
            }

            if (parentLog != null) {
                PlanForaSurfaceCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Follow-up: ${parentLog?.title}", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 1. Target Asset Section
            PlanForaSurfaceCard(title = "Primary Link (Mandatory)", isImportant = true) {
                if (selectedAssetIds.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().clickable(enabled = parentLogId == null) { showAssetPicker = true }) {
                        OutlinedTextField(
                            value = "Select Plant (Required)",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Park, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                if (parentLogId == null) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(8.dp),
                            colors = planForaTextFieldColors(isImportant = true)
                        )
                    }
                } else {
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedAssetIds.forEach { id ->
                            val asset = assets.find { it.id == id }
                            val category = AssetCategory.entries.find { it.displayName == asset?.category }
                            AssistChip(
                                onClick = { /* Could remove individual if desired */ },
                                label = { Text(asset?.name ?: "Unknown") },
                                leadingIcon = { Text(category?.icon ?: "🌿") },
                                trailingIcon = {
                                    if (parentLogId == null) {
                                        IconButton(onClick = { selectedAssetIds = selectedAssetIds - id }, modifier = Modifier.size(16.dp)) {
                                            Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                },
                                colors = AssistChipDefaults.assistChipColors(labelColor = Color.White)
                            )
                        }
                        if (parentLogId == null) {
                            AssistChip(
                                onClick = { showAssetPicker = true },
                                label = { Text("Add More") },
                                leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                ),
                                border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

            // 2. Activity Type Section
            PlanForaSurfaceCard(title = "Activity Type", isImportant = true) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activityTypes.forEach { type ->
                        FilterChip(
                            selected = activityType == type,
                            onClick = { 
                                activityType = type
                                showCustomActivityInput = type == "Other"
                            },
                            label = { Text(type, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = activityType == type,
                                borderColor = Color.Gray.copy(alpha = 0.2f),
                                selectedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                if (showCustomActivityInput) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customActivity,
                        onValueChange = { customActivity = it },
                        placeholder = { Text("Specify Activity") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(8.dp),
                        colors = planForaTextFieldColors(isImportant = true)
                    )
                }
            }

            // 3. Specialized Activity Cards
            when (activityType) {
                "Pest Control", "Feeding" -> TreatmentDetailsCard(
                    supplies = supplies,
                    selectedSupplyId = selectedSupplyId,
                    customInputName = customInputName,
                    customInputCategory = customInputCategory,
                    appMethod = appMethod,
                    dosageAmount = dosageAmount,
                    dosageRatio = dosageRatio,
                    availableZones = availableZones,
                    selectedZones = selectedZones,
                    onSupplyClick = { showSupplyBottomSheet = true },
                    onMethodChange = { appMethod = it },
                    onDosageAmountChange = { dosageAmount = it },
                    onDosageRatioChange = { dosageRatio = it },
                    onCustomInputCategoryChange = { customInputCategory = it },
                    onToggleZone = { zone ->
                        selectedZones = if (selectedZones.contains(zone)) selectedZones - zone else selectedZones + zone
                    }
                )
                "Harvest" -> HarvestActivityCard(
                    availableZones = availableZones,
                    selectedHarvestZones = selectedZones,
                    rowYields = rowYields,
                    yieldUnit = yieldUnit,
                    qualityGrade = qualityGrade,
                    onToggleHarvestZone = { zone ->
                        selectedZones = if (selectedZones.contains(zone)) selectedZones - zone else selectedZones + zone
                    },
                    onRowYieldChange = { zone, amount ->
                        val newYields = rowYields.toMutableMap()
                        newYields[zone] = amount
                        rowYields = newYields
                    },
                    onUnitChange = { yieldUnit = it },
                    onGradeChange = { qualityGrade = it }
                )
                "Repotting" -> RepottingCard(
                    substrateMix = substrateMix,
                    potSize = potSize,
                    onSubstrateChange = { substrateMix = it },
                    onPotSizeChange = { potSize = it }
                )
                "Pruning" -> PruningCard(
                    pruningType = pruningType,
                    onTypeChange = { pruningType = it }
                )
            }

            // 4. Observation Notes
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Observation Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.Gray
                ),
                placeholder = { Text("Describe your observations...", color = Color.Gray.copy(alpha = 0.6f), fontSize = 14.sp) }
            )

            // 5. Streamlined Attachments & Tags
            PlanForaSurfaceCard(title = "Attachments & Metadata", isImportant = false) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { showTagSheet = true },
                        label = { Text("Tags") },
                        leadingIcon = { Icon(Icons.Default.Tag, null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                    )

                    tags.forEach { tag ->
                        AssistChip(
                            onClick = { tags = tags - tag },
                            label = { Text(tag, fontSize = 10.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp)) },
                            colors = AssistChipDefaults.assistChipColors(labelColor = Color.White, containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // Distinct Media Section
                MediaAttachmentStrip(
                    imageUris = imageUris,
                    audioPath = audioPath,
                    onImagesAdd = { imageUris = imageUris + it },
                    onImageRemove = { imageUris = imageUris - it },
                    onAudioCaptured = { audioPath = it },
                    onAudioRemove = { audioPath = null }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // Unified Dynamic Custom Fields
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Optional Field".uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { showCustomFieldDialog = true },
                            label = { Text("Add Metric", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                        )
                    }

                    customFieldDefinitions.forEach { def ->
                        Surface(
                            color = Color(0xFF1E2120),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    fieldToManage = def
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = def.fieldName, 
                                    modifier = Modifier.weight(1f), 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp
                                )
                                Box(modifier = Modifier.weight(2f)) {
                                    DynamicCustomFieldInput(
                                        definition = def,
                                        value = customFieldValues[def.id] ?: "",
                                        onValueChange = { customFieldValues = customFieldValues.toMutableMap().apply { put(def.id, it) } }
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.archiveCustomFieldDefinition(def.id) }, 
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (showCustomFieldDialog) {
                CreateCustomFieldDialog(
                    targetType = FieldTargetType.LOG_ACTIVITY,
                    scope = activityType,
                    onDismiss = { showCustomFieldDialog = false },
                    onSave = { definition ->
                        viewModel.addCustomFieldDefinition(definition)
                        showCustomFieldDialog = false
                    }
                )
            }

            fieldToManage?.let { def ->
                ManageFieldDialog(
                    definition = def,
                    onDismiss = { fieldToManage = null },
                    onRename = { newName ->
                        viewModel.updateCustomFieldDefinition(def.copy(fieldName = newName))
                    },
                    onArchive = {
                        viewModel.archiveCustomFieldDefinition(def.id)
                    }
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        val internalUris = imageUris.mapNotNull { uri -> saveImageToInternalStorage(context, uri) }
                        
                        // Inject Activity Details into Parameters
                        when (activityType) {
                            "Pest Control", "Feeding" -> {
                                parameters["dosage"] = dosageAmount
                                parameters["ratio"] = dosageRatio
                                parameters["method"] = appMethod
                                val selectedSupply = supplies.find { it.id == selectedSupplyId }
                                if (selectedSupply?.phiDays != null && selectedSupply.phiDays!! > 0) {
                                    val phiExpiry = selectedTimestamp + (selectedSupply.phiDays!! * 24L * 60 * 60 * 1000)
                                    parameters["phi_expiry"] = phiExpiry.toString()
                                }
                                if (selectedZones.isNotEmpty()) {
                                    parameters["targeted_zones"] = selectedZones.joinToString(",")
                                }
                            }
                            "Harvest" -> {
                                if (availableZones.isEmpty()) {
                                    parameters["yield"] = rowYields["Total"] ?: ""
                                } else {
                                    val activeYields = rowYields.filterKeys { selectedZones.contains(it) }
                                    val aggregated = activeYields.values.mapNotNull { it.toDoubleOrNull() }.sum()
                                    parameters["yield"] = aggregated.toString()
                                    parameters["yield_breakdown"] = activeYields.entries.joinToString(";") { "${it.key}=${it.value}" }
                                }
                                parameters["unit"] = yieldUnit
                                parameters["grade"] = qualityGrade
                            }
                            "Repotting" -> {
                                parameters["substrate"] = substrateMix
                                parameters["pot_size"] = potSize
                            }
                            "Pruning" -> {
                                parameters["pruning_type"] = pruningType
                            }
                        }

                        val tagsString = tags.joinToString(",")
                        val paramsString = parameters.entries.joinToString("|") { "${it.key}:${it.value}" }
                        val imagesString = internalUris.joinToString(",")
                        val batchGroupId = if (selectedAssetIds.size > 1 && editingLogId == null) UUID.randomUUID().toString() else editingLog?.batchGroupId

                        if (editingLogId != null && editingLog != null) {
                            viewModel.updateLog(
                                editingLog!!.copy(
                                    title = title,
                                    note = note,
                                    tags = tagsString,
                                    parameters = paramsString,
                                    imageUris = if (imagesString.isNotBlank()) imagesString else editingLog!!.imageUris,
                                    activityType = if (activityType == "Other") customActivity else activityType,
                                    supplyId = selectedSupplyId,
                                    customInputName = if (selectedSupplyId == null) customInputName else null,
                                    targetZones = if (selectedZones.isNotEmpty()) selectedZones.joinToString(",") else null
                                )
                            )
                        } else {
                            selectedAssetIds.forEach { assetId ->
                                viewModel.addJournalLog(
                                    assetId = assetId,
                                    title = title,
                                    note = note,
                                    photoPath = null,
                                    audioFilePath = audioPath,
                                    ecValue = null,
                                    phValue = null,
                                    tags = tagsString,
                                    parameters = paramsString,
                                    imageUris = imagesString,
                                    activityType = if (activityType == "Other") customActivity else activityType,
                                    parentLogId = parentLogId,
                                    timestamp = selectedTimestamp,
                                    supplyId = selectedSupplyId,
                                    customInputName = if (selectedSupplyId == null) customInputName else null,
                                    batchGroupId = batchGroupId,
                                    targetZones = if (selectedZones.isNotEmpty()) selectedZones.joinToString(",") else null,
                                    customFieldValues = customFieldValues
                                )
                            }
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = title.isNotBlank() && selectedAssetIds.isNotEmpty(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(if (editingLogId == null) "Save Activity Log" else "Update Activity Log", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAssetPicker) {
        val masterLocations by viewModel.masterLocations.collectAsState()
        AssetPickerBottomSheet(
            assets = assets,
            masterLocations = masterLocations,
            onAssetSelected = { id ->
                id?.let { selectedAssetIds = selectedAssetIds + it }
                showAssetPicker = false
            },
            onDismiss = { showAssetPicker = false }
        )
    }

    if (showSupplyBottomSheet) {
        SupplyPickerBottomSheet(
            supplies = supplies,
            recentlyUsed = recentlyUsedSupplies,
            onSelect = { supplyId, infActivity, aiText ->
                selectedSupplyId = supplyId
                customInputName = ""
                // Only infer activity if user hasn't already picked one
                if (activityType == "Observation") {
                    activityType = infActivity
                }
                if (aiText != null && !title.contains(aiText)) title += aiText
                showSupplyBottomSheet = false
            },
            onCustomInput = {
                selectedSupplyId = null
                customInputName = "Custom Input"
                showSupplyBottomSheet = false
            },
            onDismiss = { showSupplyBottomSheet = false }
        )
    }

    if (showTagSheet) {
        TagPickerSheet(
            masterTags = masterTags,
            selectedTags = tags,
            onTagToggle = { tag -> tags = if (tags.contains(tag)) tags - tag else tags + tag },
            onNewTagCreated = { viewModel.addMasterTag(it); tags = tags + it },
            onRenameTag = { old, new -> 
                viewModel.updateMasterTag(old, new)
                if (tags.contains(old)) tags = tags - old + new
            },
            onDeleteTag = { tag -> 
                viewModel.deleteMasterTag(tag)
                tags = tags - tag
            },
            onDismiss = { showTagSheet = false }
        )
    }
}

@Composable
fun TreatmentDetailsCard(
    supplies: List<DiySupplyEntity>,
    selectedSupplyId: Long?,
    customInputName: String,
    customInputCategory: String,
    appMethod: String,
    dosageAmount: String,
    dosageRatio: String,
    availableZones: List<String>,
    selectedZones: Set<String>,
    onSupplyClick: () -> Unit,
    onMethodChange: (String) -> Unit,
    onDosageAmountChange: (String) -> Unit,
    onDosageRatioChange: (String) -> Unit,
    onCustomInputCategoryChange: (String) -> Unit,
    onToggleZone: (String) -> Unit
) {
    PlanForaSurfaceCard(title = "Treatment Details", isImportant = true) {
        val selectedSupply = supplies.find { it.id == selectedSupplyId }
        
        if (availableZones.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Target Zones / Rows", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableZones.forEach { zone ->
                        val isSelected = selectedZones.contains(zone)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleZone(zone) },
                            label = { Text(zone, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.Gray.copy(alpha = 0.2f),
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(
                value = selectedSupply?.batchCode ?: customInputName.ifBlank { "Select Product" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSupplyClick() },
                enabled = false,
                leadingIcon = { Icon(Icons.Default.Science, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = planForaTextFieldColors(isImportant = true)
            )

            AnimatedVisibility(
                visible = selectedSupply?.notes?.isNotBlank() == true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedSupply?.notes?.let { notes ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dosage/Note: $notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (selectedSupplyId == null && customInputName.isNotBlank()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Pesticide", "Fungicide", "Organic").forEach { cat ->
                    FilterChip(
                        selected = customInputCategory == cat,
                        onClick = { onCustomInputCategoryChange(cat) },
                        label = { Text(cat, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = customInputCategory == cat,
                            borderColor = Color.Gray.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        PlanForaFieldGroup(isImportant = true) {
            listOf("FOLIAR SPRAY", "SOIL DRENCH", "SPOT").forEach { method ->
                val isSelected = appMethod == method
                Surface(
                    onClick = { onMethodChange(method) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    border = if (!isSelected) BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline) else null
                ) {
                    Text(method, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        PlanForaFieldGroup(isImportant = true) {
            OutlinedTextField(value = dosageAmount, onValueChange = onDosageAmountChange, label = { Text("Amount") }, modifier = Modifier.weight(1f), singleLine = true, colors = planForaTextFieldColors(isImportant = true))
            OutlinedTextField(value = dosageRatio, onValueChange = onDosageRatioChange, label = { Text("Unit/Ratio") }, modifier = Modifier.weight(1.5f), singleLine = true, colors = planForaTextFieldColors(isImportant = true))
        }
    }
}

@Composable
fun RepottingCard(substrateMix: String, potSize: String, onSubstrateChange: (String) -> Unit, onPotSizeChange: (String) -> Unit) {
    PlanForaSurfaceCard(title = "Substrate & Container", isImportant = false) {
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.LightGray,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = Color.Gray
        )
        OutlinedTextField(value = substrateMix, onValueChange = onSubstrateChange, label = { Text("Substrate Mix") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("e.g. Coco/Perlite 70/30") }, colors = fieldColors)
        OutlinedTextField(value = potSize, onValueChange = onPotSizeChange, label = { Text("Pot Size / Bed ID") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
    }
}

@Composable
fun PruningCard(pruningType: String, onTypeChange: (String) -> Unit) {
    PlanForaSurfaceCard(title = "Pruning Type", isImportant = false) {
        PlanForaFieldGroup(isImportant = false) {
            listOf("Sanitary", "Structural", "Thinning").forEach { type ->
                FilterChip(
                    selected = pruningType == type,
                    onClick = { onTypeChange(type) },
                    label = { Text(type) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = pruningType == type,
                        borderColor = Color.Gray.copy(alpha = 0.2f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplyPickerBottomSheet(
    supplies: List<DiySupplyEntity>,
    recentlyUsed: List<DiySupplyEntity>,
    onSelect: (Long, String, String?) -> Unit,
    onCustomInput: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = supplies.filter { it.batchCode.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF1E2120)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Search Supplies", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("🔍 Search...") }, modifier = Modifier.fillMaxWidth())
            LazyColumn(modifier = Modifier.fillMaxHeight(0.7f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Surface(
                        onClick = onCustomInput,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("One-off Custom Input", color = Color.White)
                        }
                    }
                }
                if (recentlyUsed.isNotEmpty() && searchQuery.isBlank()) {
                    item { Text("RECENTLY USED", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                    items(recentlyUsed) { s -> SupplyItemRow(s) {
                        val activity = when {
                            s.category.contains("icide", ignoreCase = true) -> "Pest Control"
                            s.category.equals("Fertilizer", ignoreCase = true) -> "Feeding"
                            else -> "Observation"
                        }
                        val ai = s.activeIngredient?.let { " [A.I.: $it]" }
                        onSelect(s.id, activity, ai)
                    } }
                }
                item { Text("ALL SUPPLIES", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                items(filtered) { s -> SupplyItemRow(s) {
                    val activity = when {
                        s.category.contains("icide", ignoreCase = true) -> "Pest Control"
                        s.category.equals("Fertilizer", ignoreCase = true) -> "Feeding"
                        else -> "Observation"
                    }
                    val ai = s.activeIngredient?.let { " [A.I.: $it]" }
                    onSelect(s.id, activity, ai)
                } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetPickerBottomSheet(
    assets: List<PlantAssetEntity>,
    masterLocations: List<String>,
    onAssetSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AssetCategory.ALL) }
    var selectedLocation by remember { mutableStateOf("All") }

    val filteredAssets = assets.filter { asset ->
        (selectedCategory == AssetCategory.ALL || asset.category == selectedCategory.displayName) &&
        (selectedLocation == "All" || asset.locationNote == selectedLocation) &&
        (asset.name.contains(searchQuery, ignoreCase = true) || 
         asset.locationNote.contains(searchQuery, ignoreCase = true) ||
         asset.tags.contains(searchQuery, ignoreCase = true))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2120),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(0.85f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Select Plant / Asset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("🔍 Search...") }, modifier = Modifier.fillMaxWidth())
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AssetCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.icon} ${cat.displayName}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == cat,
                            borderColor = Color.Gray.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedLocation == "All",
                        onClick = { selectedLocation = "All" },
                        label = { Text("All Locations") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedLocation == "All",
                            borderColor = Color.Gray.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
                items(masterLocations) { loc ->
                    FilterChip(
                        selected = selectedLocation == loc,
                        onClick = { selectedLocation = loc },
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
                            borderColor = Color.Gray.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Surface(
                        onClick = { onAssetSelected(null) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("None / General Log", color = Color.White, modifier = Modifier.padding(16.dp))
                    }
                }
                items(filteredAssets) { asset ->
                    val catIcon = AssetCategory.entries.find { it.displayName == asset.category }?.icon ?: "🌿"
                    Surface(
                        onClick = { onAssetSelected(asset.id) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Text(catIcon, fontSize = 20.sp) }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(asset.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(asset.locationNote.ifBlank { "Unassigned" }, color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplyItemRow(supply: DiySupplyEntity, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(supply.batchCode, color = Color.White, fontWeight = FontWeight.Bold)
                if (!supply.activeIngredient.isNullOrBlank()) Text("A.I.: ${supply.activeIngredient}", color = Color.Gray, fontSize = 12.sp)
            }
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) { Text(supply.category, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
        }
    }
}

private fun saveImageToInternalStorage(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        inputStream?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
        file.absolutePath
    } catch (e: Exception) { null }
}
