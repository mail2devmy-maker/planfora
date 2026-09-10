package com.mail2dev.planfora.ui.assets

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
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
import com.mail2dev.planfora.ui.theme.*
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

            // Live Preview Card
            LivePreviewCard(name, selectedCategory, location, selectedTags)

            // Asset Identity
            PlanForaSurfaceCard(title = "Asset Identity", isImportant = true) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Asset Name / Variety",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextPrimary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = viewModel::updateName,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(isImportant = true)
                    )
                }
                CategorySelector(selectedCategory, viewModel::updateCategory)
            }

            // Location & Tags
            PlanForaSurfaceCard(title = "Location & Tags", isImportant = false) {
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

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Asset Notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextSecondary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = viewModel::updateNotes,
                        placeholder = { Text("Remark for asset or asset detail...") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = textFieldColors(isImportant = false),
                        maxLines = 3
                    )
                }
            }

            // Optional Field Section (EAV style)
            PlanForaSurfaceCard(title = "Metrics", isImportant = false) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { showCustomFieldDialog = true },
                        label = { Text("Optional Fields", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                        ),
                        border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                    )
                }

                DynamicFieldRenderer(viewModel, customFieldDefinitions, customFieldValues)
            }

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
fun LivePreviewCard(name: String, category: AssetCategory, location: String, tags: Set<String>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (category.iconVector != null) {
                    Icon(
                        imageVector = category.iconVector,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(category.icon, fontSize = 24.sp)
                }
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

@Composable
fun CategorySelector(selected: AssetCategory, onSelect: (AssetCategory) -> Unit) {
    val categories = AssetCategory.entries.filter { it != AssetCategory.ALL }
    
    Column {
        Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { cat ->
                        FilterChip(
                            selected = selected == cat,
                            onClick = { onSelect(cat) },
                            modifier = Modifier.weight(1f),
                            label = { 
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (cat.iconVector != null) {
                                        Icon(
                                            imageVector = cat.iconVector,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp).padding(end = 4.dp),
                                            tint = if (selected == cat) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(cat.icon, modifier = Modifier.padding(end = 4.dp))
                                    }
                                    Text(cat.displayName, fontSize = 12.sp)
                                }
                            },
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
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReadonlyTriggerField(label: String, value: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, isImportant: Boolean = false) {
    val borderColor = if (isImportant) MandatoryBorder else OptionalBorder
    val labelColor = if (isImportant) SlateTextPrimary else SlateTextSecondary

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent)
                .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = value,
                    color = if (value == "Select" || value == "Select") Color.Gray else Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicFieldRenderer(
    viewModel: AddAssetViewModel,
    customFields: List<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity>,
    customValues: Map<Long, String>
) {
    var fieldToManage by remember { mutableStateOf<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity?>(null) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        customFields.forEach { def ->
            Surface(
                color = Color(0xFF1E2120),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth().combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                            value = customValues[def.id] ?: "",
                            onValueChange = { viewModel.updateCustomFieldValue(def.id, it) }
                        )
                    }
                    IconButton(onClick = { viewModel.archiveCustomFieldDefinition(def.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
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

@Composable
fun SimpleTextField(label: String, value: String, onValueChange: (String) -> Unit, isImportant: Boolean = true) {
    val labelColor = if (isImportant) SlateTextPrimary else SlateTextSecondary
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors(isImportant = isImportant)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, value: Long?, onDateSelected: (Long?) -> Unit, isImportant: Boolean = true) {
    val labelColor = if (isImportant) SlateTextPrimary else SlateTextSecondary
    var showPicker by remember { mutableStateOf(false) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = configuration.locales[0]
    val dateDisplay = remember(value, locale) {
        if (value != null) SimpleDateFormat("MMM dd, yyyy", locale).format(Date(value)) else ""
    }
    
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp)
        )
        OutlinedTextField(
            value = dateDisplay,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = textFieldColors(isImportant = isImportant)
        )
    }
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
