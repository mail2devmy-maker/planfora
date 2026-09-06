package com.mail2dev.planfora.ui.assets

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.mail2dev.planfora.ui.components.LocationSelectionBottomSheet
import com.mail2dev.planfora.ui.components.MediaAttachmentStrip
import com.mail2dev.planfora.ui.components.PlanForaFieldGroup
import com.mail2dev.planfora.ui.components.PlanForaSurfaceCard
import com.mail2dev.planfora.ui.components.TagPickerSheet
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Live Preview Card (Memento Style)
            LivePreviewCard(name, selectedCategory, location, selectedTags)

            // Asset Identity
            PlanForaSurfaceCard(title = "Asset Identity") {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Asset Name / Variety") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                CategorySelector(selectedCategory, viewModel::updateCategory)
            }

            // Location & Taxonomy
            PlanForaSurfaceCard(title = "Location & Taxonomy") {
                PlanForaFieldGroup {
                    ReadonlyTriggerField(
                        label = "Location",
                        value = location.ifBlank { "Select" },
                        icon = Icons.Default.LocationOn,
                        onClick = { showLocationSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                    ReadonlyTriggerField(
                        label = "Tags",
                        value = if (selectedTags.isEmpty()) "Select" else "${selectedTags.size} tags",
                        icon = Icons.Default.Tag,
                        onClick = { showTagSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Lifecycle Details
            if (selectedCategory != AssetCategory.ALL) {
                PlanForaSurfaceCard(title = "${selectedCategory.displayName} Details") {
                    CategoryCoreFields(selectedCategory, viewModel)
                }
            }

            // Attachments & Notes
            PlanForaSurfaceCard(title = "Attachments & Notes") {
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
                    colors = textFieldColors(),
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
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
        CustomFieldCreatorDialog(
            onDismiss = { showCustomFieldDialog = false },
            onFieldCreated = { name, type, options, isGlobal ->
                viewModel.addCustomFieldDefinition(name, type, options, isGlobal)
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
        color = SageGreen.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.2f))
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
                        color = SageGreen
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
        Text("Category", style = MaterialTheme.typography.labelLarge, color = SageGreen)
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
                        selectedContainerColor = SageGreen,
                        selectedLabelColor = DarkBackground,
                        containerColor = Color.Transparent,
                        labelColor = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun ReadonlyTriggerField(label: String, value: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable { onClick() },
        leadingIcon = { Icon(icon, contentDescription = null, tint = SageGreen, modifier = Modifier.size(18.dp)) },
        trailingIcon = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.White,
            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
            disabledLabelColor = SageGreen,
            disabledLeadingIconColor = SageGreen
        )
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
        Text("Quick-Add Milestones & Metrics", style = MaterialTheme.typography.labelLarge, color = SageGreen, fontWeight = FontWeight.SemiBold)
        
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
                        selectedContainerColor = SageGreen,
                        selectedLabelColor = DarkBackground,
                        labelColor = Color.Gray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isVisible,
                        borderColor = Color.Gray.copy(alpha = 0.3f),
                        selectedBorderColor = SageGreen
                    )
                )
            }

            AssistChip(
                onClick = onAddCustomField,
                label = { Text("Custom", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(labelColor = SageGreen),
                border = BorderStroke(0.5.dp, SageGreen.copy(alpha = 0.5f))
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

@Composable
fun DynamicFieldRenderer(
    viewModel: AddAssetViewModel,
    visibleFields: Set<OptionalField>,
    customFields: List<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity>,
    customValues: Map<Long, String>,
    category: AssetCategory
) {
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
        customFields.filter { it.category == category.displayName || it.category == "Global" }.forEach { def ->
            MementoLedgerRow(
                label = def.fieldName,
                onRemove = { /* Logic to hide custom field if needed */ }
            ) {
                CustomFieldInputCompact(
                    definition = def,
                    value = customValues[def.id] ?: "",
                    onValueChange = { viewModel.updateCustomFieldValue(def.id, it) }
                )
            }
        }
    }
}

@Composable
fun MementoLedgerRow(
    label: String,
    onRemove: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = Color(0xFF1E2120),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            content()
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun RowScope.CustomFieldInputCompact(
    definition: com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity,
    value: String,
    onValueChange: (String) -> Unit
) {
    when (definition.fieldType) {
        "RADIO" -> {
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1.5f)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                    enabled = false,
                    placeholder = { Text("Select", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = Color.Transparent
                    )
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    definition.radioOptionsJson?.split(",")?.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt) }, onClick = { onValueChange(opt); expanded = false })
                    }
                }
            }
        }
        "NUMBER" -> {
            SimpleTextFieldCompact(value, onValueChange, isNumber = true)
        }
        else -> {
            SimpleTextFieldCompact(value, onValueChange)
        }
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
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = SageGreen,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun RowScope.DatePickerFieldCompact(value: Long?, onDateSelected: (Long?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val dateDisplay = if (value != null) SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(value)) else "Select Date"
    
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
fun CustomFieldCreatorDialog(onDismiss: () -> Unit, onFieldCreated: (String, String, String?, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("TEXT") }
    var options by remember { mutableStateOf("") }
    var isGlobal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2120),
        title = { Text("New Custom Field", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Field Name") }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isGlobal, onCheckedChange = { isGlobal = it })
                    Text("Global Field (All categories)", color = Color.LightGray, fontSize = 12.sp)
                }

                Text("Field Type", color = Color.Gray, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TEXT", "NUMBER", "RADIO").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SageGreen))
                    }
                }
                if (type == "RADIO") {
                    OutlinedTextField(value = options, onValueChange = { options = it }, label = { Text("Options (comma separated)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(onClick = { onFieldCreated(name, type, options.ifBlank { null }, isGlobal) }, enabled = name.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = SageGreen)) {
                Text("Create", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun SimpleTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = textFieldColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, value: Long?, onDateSelected: (Long?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val dateDisplay = if (value != null) SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(value)) else ""
    
    OutlinedTextField(
        value = dateDisplay,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SageGreen)
            }
        },
        colors = textFieldColors()
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
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SageGreen,
    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
    focusedLabelColor = SageGreen
)
