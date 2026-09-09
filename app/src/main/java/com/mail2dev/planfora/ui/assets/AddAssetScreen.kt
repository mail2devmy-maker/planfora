package com.mail2dev.planfora.ui.assets

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mail2dev.planfora.ui.components.CreateCustomFieldDialog
import com.mail2dev.planfora.ui.components.DynamicCustomFieldInput
import com.mail2dev.planfora.ui.components.LocationSelectionBottomSheet
import com.mail2dev.planfora.ui.components.ManageFieldDialog
import com.mail2dev.planfora.ui.components.MediaAttachmentStrip
import com.mail2dev.planfora.ui.components.PlanForaFieldGroup
import com.mail2dev.planfora.ui.components.PlanForaSurfaceCard
import com.mail2dev.planfora.ui.components.TagPickerSheet
import com.mail2dev.planfora.ui.components.planForaTextFieldColors
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import com.mail2dev.planfora.ui.theme.ForestEmerald
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAssetScreen(
    viewModel: AddAssetViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val location by viewModel.location.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val visibleOptionalFields by viewModel.visibleOptionalFields.collectAsState()
    val imageUris by viewModel.imageUris.collectAsState()
    val audioPath by viewModel.audioPath.collectAsState()
    val customFieldDefinitions by viewModel.customFieldDefinitions.collectAsState()
    val customFieldValues by viewModel.customFieldValues.collectAsState()
    val editingAssetId by viewModel.editingAssetId.collectAsState()

    var showLocationSheet by remember { mutableStateOf(false) }
    var showTagSheet by remember { mutableStateOf(false) }
    var showCustomFieldDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = remember(name, location, selectedTags, notes, imageUris, audioPath) {
        name.isNotBlank() || location.isNotBlank() || selectedTags.isNotEmpty() || notes.isNotBlank() || imageUris.isNotEmpty() || audioPath != null
    }

    BackHandler(enabled = hasUnsavedChanges && editingAssetId == null) {
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
                    onDismiss() 
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            if (hasUnsavedChanges && editingAssetId == null) {
                showDiscardDialog = true
            } else {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (editingAssetId == null) "New Plant Profile" else "Edit Plant Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    if (hasUnsavedChanges && editingAssetId == null) {
                        showDiscardDialog = true
                    } else {
                        onDismiss()
                    }
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Live Preview Card (Memento Style)
            LivePreviewCard(name, selectedCategory, location, selectedTags)

            // Asset Identity
            PlanForaSurfaceCard(title = "Asset Identity", isImportant = true) {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Asset Name / Variety") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(isImportant = true)
                )
                CategorySelector(selectedCategory, viewModel::updateCategory)
            }

            // Location & Taxonomy
            PlanForaSurfaceCard(title = "Location & Taxonomy", isImportant = false) {
                PlanForaFieldGroup(isImportant = false) {
                    ReadonlyTriggerField(
                        label = "Location",
                        value = location.ifBlank { "Select" },
                        icon = Icons.Default.LocationOn,
                        onClick = { showLocationSheet = true },
                        modifier = Modifier.weight(1f),
                        isImportant = false
                    )
                    ReadonlyTriggerField(
                        label = "Tags",
                        value = if (selectedTags.isEmpty()) "Select" else "${selectedTags.size} tags",
                        icon = Icons.Default.Tag,
                        onClick = { showTagSheet = true },
                        modifier = Modifier.weight(1f),
                        isImportant = false
                    )
                }
            }

            // Lifecycle Details
            if (selectedCategory != AssetCategory.ALL) {
                PlanForaSurfaceCard(title = "${selectedCategory.displayName} Details", isImportant = true) {
                    CategoryCoreFields(selectedCategory, viewModel)
                }
            }

            // Attachments & Notes
            PlanForaSurfaceCard(title = "Attachments & Notes", isImportant = false) {
                MediaAttachmentStrip(
                    imageUris = imageUris,
                    audioPath = audioPath,
                    onImagesAdd = { uris -> uris.forEach { viewModel.addImageUri(it) } },
                    onImageRemove = { viewModel.removeImageUri(it) },
                    onAudioCaptured = { viewModel.setAudioPath(it) },
                    onAudioRemove = { viewModel.setAudioPath(null) }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Asset Notes") },
                    placeholder = { Text("Quick observation or asset details...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = textFieldColors(isImportant = false),
                    maxLines = 3
                )
            }

            // Optional Field Palette
            OptionalFieldPalette(
                selectedCategory = selectedCategory,
                visibleFields = visibleOptionalFields,
                onToggleField = viewModel::toggleOptionalField,
                onAddCustomField = { showCustomFieldDialog = true }
            )

            // Dynamic Optional Inputs (Built-in + Custom)
            DynamicFieldRenderer(viewModel, visibleOptionalFields, customFieldDefinitions, customFieldValues, selectedCategory)

            Button(
                onClick = {
                    viewModel.saveAsset()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (editingAssetId == null) "Create Asset Profile" else "Update Asset Profile",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showLocationSheet) {
        val masterLocations by viewModel.masterLocations.collectAsState()
        LocationSelectionBottomSheet(
            selectedLocation = location,
            masterLocations = masterLocations,
            onLocationSelected = viewModel::updateLocation,
            onNewLocationCreated = viewModel::addMasterLocation,
            onRenameLocation = viewModel::updateMasterLocation,
            onDeleteLocation = viewModel::deleteMasterLocation,
            onDismiss = { showLocationSheet = false }
        )
    }

    if (showTagSheet) {
        val masterTags by viewModel.masterTags.collectAsState()
        TagPickerSheet(
            masterTags = masterTags,
            selectedTags = selectedTags,
            onTagToggle = viewModel::toggleTag,
            onNewTagCreated = viewModel::addMasterTag,
            onRenameTag = viewModel::updateMasterTag,
            onDeleteTag = viewModel::deleteMasterTag,
            onDismiss = { showTagSheet = false }
        )
    }

    if (showCustomFieldDialog) {
        CreateCustomFieldDialog(
            targetType = FieldTargetType.ASSET_CATEGORY,
            scope = selectedCategory.displayName,
            onDismiss = { showCustomFieldDialog = false },
            onSave = { definition ->
                viewModel.addCustomFieldDefinition(definition)
                showCustomFieldDialog = false
            }
        )
    }
}

@Composable
fun CategoryCoreFields(category: AssetCategory, viewModel: AddAssetViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (category) {
            AssetCategory.SEEDLING -> {
                SimpleTextField("Batch / Tray ID", viewModel.batchTrayId.collectAsState().value, viewModel::updateBatchTrayId)
                SimpleTextField("Quantity", viewModel.quantity.collectAsState().value, viewModel::updateQuantity)
                SimpleTextField("Logical Zones / Rows (e.g. Row 1, Row 2)", viewModel.zones.collectAsState().value, viewModel::updateZones)
            }
            AssetCategory.CUTTING -> {
                SimpleTextField("Mother Plant Link", viewModel.motherPlantLink.collectAsState().value, viewModel::updateMotherPlantLink)
                DatePickerField("Propagated Date", viewModel.propagatedDate.collectAsState().value, viewModel::updatePropagatedDate)
            }
            AssetCategory.TREE -> {
                SimpleTextField("Physical ID / Tree #", viewModel.physicalId.collectAsState().value, viewModel::updatePhysicalId)
            }
            AssetCategory.CROP_OR_VEGGIE -> {
                SimpleTextField("Plot / Field ID", viewModel.plotRowId.collectAsState().value, viewModel::updatePlotRowId)
                SimpleTextField("Logical Zones / Rows (e.g. Row 1, Row 2)", viewModel.zones.collectAsState().value, viewModel::updateZones)
                DatePickerField("Expected Harvest Date", viewModel.expectedHarvestDate.collectAsState().value, viewModel::updateExpectedHarvestDate)
            }
            else -> {}
        }
    }
}

@Composable
fun HeaderSection(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "New Plant Profile",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
        }
    }
}

@Composable
fun LivePreviewCard(name: String, category: AssetCategory, location: String, tags: Set<String>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(category.icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name.ifBlank { "Unnamed Asset" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${category.displayName} • 📍 ${location.ifBlank { "Unassigned" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        Text("#$tag", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(selected: AssetCategory, onSelect: (AssetCategory) -> Unit) {
    Column {
        Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssetCategory.entries.filter { it != AssetCategory.ALL }.forEach { cat ->
                FilterChip(
                    selected = selected == cat,
                    onClick = { onSelect(cat) },
                    label = { Text("${cat.icon} ${cat.displayName}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected == cat,
                        borderColor = Color.Gray.copy(alpha = 0.2f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun ReadonlyTriggerField(label: String, value: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, isImportant: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable { onClick() },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
        trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
        enabled = false,
        colors = textFieldColors(isImportant = isImportant)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionalFieldPalette(
    selectedCategory: AssetCategory,
    visibleFields: Set<OptionalField>,
    onToggleField: (OptionalField) -> Unit,
    onAddCustomField: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Optional Fields", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OptionalField.entries.filter { isRelevant(it, selectedCategory) }.forEach { field ->
                val isVisible = visibleFields.contains(field)
                
                FilterChip(
                    selected = isVisible,
                    onClick = { onToggleField(field) },
                    label = { Text("+ ${field.displayName}", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isVisible,
                        borderColor = Color.Gray.copy(alpha = 0.2f),
                        selectedBorderColor = Color.Transparent
                    )
                )
            }

            AssistChip(
                onClick = onAddCustomField,
                label = { Text("Custom", fontSize = 11.sp) },
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

private fun isRelevant(field: OptionalField, category: AssetCategory): Boolean {
    return when (category) {
        AssetCategory.SEEDLING -> field != OptionalField.MOTHER_PLANT_LINK && field != OptionalField.PHYSICAL_ID && field != OptionalField.ROOTSTOCK && field != OptionalField.BATCH_TRAY_ID && field != OptionalField.QUANTITY && field != OptionalField.PLOT_ROW_ID && field != OptionalField.EXPECTED_HARVEST_DATE && field != OptionalField.PROPAGATED_DATE
        AssetCategory.CUTTING -> field != OptionalField.PHYSICAL_ID && field != OptionalField.ROOTSTOCK && field != OptionalField.MOTHER_PLANT_LINK && field != OptionalField.PROPAGATED_DATE && field != OptionalField.BATCH_TRAY_ID && field != OptionalField.QUANTITY && field != OptionalField.PLOT_ROW_ID && field != OptionalField.EXPECTED_HARVEST_DATE
        AssetCategory.TREE -> field != OptionalField.BATCH_TRAY_ID && field != OptionalField.QUANTITY && field != OptionalField.PLOT_ROW_ID && field != OptionalField.EXPECTED_HARVEST_DATE && field != OptionalField.PHYSICAL_ID && field != OptionalField.MOTHER_PLANT_LINK && field != OptionalField.PROPAGATED_DATE
        AssetCategory.CROP_OR_VEGGIE -> field != OptionalField.MOTHER_PLANT_LINK && field != OptionalField.ROOTSTOCK && field != OptionalField.PHYSICAL_ID && field != OptionalField.PLOT_ROW_ID && field != OptionalField.EXPECTED_HARVEST_DATE && field != OptionalField.BATCH_TRAY_ID && field != OptionalField.QUANTITY && field != OptionalField.PROPAGATED_DATE
        else -> true
    }
}

private fun isSuggested(field: OptionalField, category: AssetCategory): Boolean {
    return when (category) {
        AssetCategory.TREE -> field == OptionalField.PLANTING_DATE || field == OptionalField.GPS_COORDINATES
        else -> false
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicFieldRenderer(
    viewModel: AddAssetViewModel,
    visibleFields: Set<OptionalField>,
    customFields: List<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity>,
    customValues: Map<Long, String>,
    category: AssetCategory
) {
    var fieldToManage by remember { mutableStateOf<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity?>(null) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        visibleFields.forEach { field ->
            MementoLedgerRow(
                label = field.displayName,
                onRemove = { viewModel.toggleOptionalField(field) }
            ) {
                when (field) {
                    OptionalField.PLANTING_DATE -> DatePickerFieldCompact(viewModel.plantedDate.collectAsState().value, viewModel::updatePlantedDate)
                    OptionalField.ACQUISITION_DETAILS -> DatePickerFieldCompact(viewModel.acquisitionDate.collectAsState().value, viewModel::updateAcquisitionDate)
                    OptionalField.COST_VALUE -> {
                        SimpleTextFieldCompact(viewModel.costValue.collectAsState().value, viewModel::updateCostValue)
                    }
                    OptionalField.ROOTSTOCK -> {
                        SimpleTextFieldCompact(viewModel.rootstock.collectAsState().value, viewModel::updateRootstock)
                    }
                    OptionalField.GPS_COORDINATES -> {
                        SimpleTextFieldCompact("", { /* TODO */ })
                    }
                    else -> {}
                }
            }
        }

        // Render Custom Fields (Scoped to Category)
        customFields.forEach { def ->
            MementoLedgerRow(
                label = def.fieldName,
                onRemove = { viewModel.archiveCustomFieldDefinition(def.id) },
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        fieldToManage = def
                    }
                )
            ) {
                Box(modifier = Modifier.weight(2f)) {
                    DynamicCustomFieldInput(
                        definition = def,
                        value = customValues[def.id] ?: "",
                        onValueChange = { viewModel.updateCustomFieldValue(def.id, it) }
                    )
                }
            }
        }
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
}

@Composable
fun RowScope.SimpleTextFieldCompact(value: String, onValueChange: (String) -> Unit, isNumber: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().weight(1.5f),
        placeholder = { Text("Enter...", fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = if (isNumber) androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number) else androidx.compose.foundation.text.KeyboardOptions.Default,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.DatePickerFieldCompact(value: Long?, onDateSelected: (Long?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = configuration.locales[0]
    val dateDisplay = remember(value, locale) {
        if (value != null) SimpleDateFormat("MMM dd, yyyy", locale).format(Date(value)) else "Select Date"
    }
    
    OutlinedTextField(
        value = dateDisplay,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth().weight(1.5f).clickable { showPicker = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = if (value != null) Color.White else Color.Gray,
            disabledBorderColor = Color.Transparent
        )
    )
    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateSelected(null)
                    showPicker = false
                }) { Text("Clear") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun MementoLedgerRow(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = Color(0xFF1E2120),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().then(modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            content()
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SimpleTextField(label: String, value: String, onValueChange: (String) -> Unit, isImportant: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = textFieldColors(isImportant = isImportant)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, value: Long?, onDateSelected: (Long?) -> Unit, isImportant: Boolean = true) {
    var showPicker by remember { mutableStateOf(false) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = configuration.locales[0]
    val dateDisplay = remember(value, locale) {
        if (value != null) SimpleDateFormat("MMM dd, yyyy", locale).format(Date(value)) else ""
    }
    
    OutlinedTextField(
        value = dateDisplay,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        colors = textFieldColors(isImportant = isImportant)
    )
    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDateSelected(null)
                    showPicker = false
                }) { Text("Clear") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun textFieldColors(isImportant: Boolean = false) = planForaTextFieldColors(isImportant)
