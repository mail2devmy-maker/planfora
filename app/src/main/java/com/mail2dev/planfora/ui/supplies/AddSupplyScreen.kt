package com.mail2dev.planfora.ui.supplies

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.mail2dev.planfora.data.local.entity.SupplyFormType
import com.mail2dev.planfora.ui.components.LocationSelectionBottomSheet
import com.mail2dev.planfora.ui.components.MediaAttachmentStrip
import com.mail2dev.planfora.ui.components.PlanForaFieldGroup
import com.mail2dev.planfora.ui.components.PlanForaSurfaceCard
import com.mail2dev.planfora.ui.components.StringPickerSheet
import com.mail2dev.planfora.ui.components.TagPickerSheet
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
import com.mail2dev.planfora.ui.assets.CustomFieldInputCompact

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSupplyScreen(
    viewModel: AddSupplyViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val category by viewModel.category.collectAsState()
    val formType by viewModel.formType.collectAsState()
    val formulationCode by viewModel.formulationCode.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val location by viewModel.location.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val imageUris by viewModel.imageUris.collectAsState()
    val visibleOptionalFields by viewModel.visibleOptionalFields.collectAsState()
    val customFieldDefinitions by viewModel.customFieldDefinitions.collectAsState()
    val customFieldValues by viewModel.customFieldValues.collectAsState()
    val editingSupplyId by viewModel.editingSupplyId.collectAsState()

    val context = LocalContext.current
    var showTagSheet by remember { mutableStateOf(false) }
    var showCustomFieldDialog by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }

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
                    if (editingSupplyId == null) "New Product / Supply" else "Edit Product / Supply",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Core Name Field
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            // Primary Identification & Storage
            PlanForaSurfaceCard(title = "Identification & Storage") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelLarge, color = SageGreen)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SupplyCategory.entries.filter { it != SupplyCategory.ALL }.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { viewModel.updateCategory(cat) },
                                label = { Text(cat.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreen,
                                    selectedLabelColor = DarkBackground,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Gray
                                )
                            )
                        }
                    }

                    PlanForaFieldGroup {
                        ReadonlyTriggerField(
                            label = "📍 Storage Location",
                            value = location.ifBlank { "Select Location" },
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
            }

            // High-Speed Safety/Stock Fields
            PlanForaSurfaceCard(title = "Stock & Safety") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PlanForaFieldGroup {
                        var showIngredientSheet by remember { mutableStateOf(false) }
                        val activeIngredientValue = viewModel.activeIngredient.collectAsState().value
                        
                        ReadonlyTriggerField(
                            label = "Active Ingredient",
                            value = activeIngredientValue.ifBlank { "Select A.I." },
                            icon = Icons.Default.Tag,
                            onClick = { showIngredientSheet = true },
                            modifier = Modifier.weight(1.5f)
                        )

                        if (showIngredientSheet) {
                            val masterIngredients by viewModel.masterIngredients.collectAsState()
                            StringPickerSheet(
                                title = "Active Ingredient",
                                selectedValue = activeIngredientValue,
                                items = masterIngredients,
                                onItemSelected = viewModel::updateActiveIngredient,
                                onItemCreated = viewModel::addMasterIngredient,
                                onItemRenamed = viewModel::updateMasterIngredient,
                                onItemDeleted = viewModel::deleteMasterIngredient,
                                onDismiss = { showIngredientSheet = false },
                                placeholder = "Search or type chemical...",
                                addLabel = "Add"
                            )
                        }
                        
                        OutlinedTextField(
                            value = viewModel.phiDays.collectAsState().value,
                            onValueChange = viewModel::updatePhiDays,
                            label = { Text("PHI (Days)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = textFieldColors()
                        )
                    }

                    PlanForaFieldGroup {
                        OutlinedTextField(
                            value = viewModel.stockQuantity.collectAsState().value,
                            onValueChange = viewModel::updateStockQuantity,
                            label = { Text("Stock Amount") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = textFieldColors()
                        )
                        var showUnitPicker by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = viewModel.stockUnit.collectAsState().value,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { showUnitPicker = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null, tint = SageGreen)
                                    }
                                },
                                colors = textFieldColors()
                            )
                            DropdownMenu(expanded = showUnitPicker, onDismissRequest = { showUnitPicker = false }) {
                                listOf("L", "mL", "kg", "g", "units", "bottles").forEach { unit ->
                                    DropdownMenuItem(text = { Text(unit) }, onClick = { viewModel.updateStockUnit(unit); showUnitPicker = false })
                                }
                            }
                        }
                    }
                }
            }

            // Product Formulation Row
            PlanForaSurfaceCard(title = "Product Formulation") {
                PlanForaFieldGroup {
                    var showFormTypePicker by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = formType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Form / Type") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showFormTypePicker = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = SageGreen)
                                }
                            },
                            colors = textFieldColors()
                        )
                        DropdownMenu(expanded = showFormTypePicker, onDismissRequest = { showFormTypePicker = false }) {
                            SupplyFormType.entries.forEach { type ->
                                DropdownMenuItem(text = { Text(type.displayName) }, onClick = { viewModel.updateFormType(type); showFormTypePicker = false })
                            }
                        }
                    }

                    if (category != SupplyCategory.HARDWARE && category != SupplyCategory.SUBSTRATE) {
                        var showFormulationPicker by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = formulationCode ?: "N/A",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Form. Code") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { showFormulationPicker = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null, tint = SageGreen)
                                    }
                                },
                                colors = textFieldColors()
                            )
                            DropdownMenu(expanded = showFormulationPicker, onDismissRequest = { showFormulationPicker = false }) {
                                val availableCodes = when (formType) {
                                    com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID -> listOf("SL", "SC", "EC", "Other")
                                    com.mail2dev.planfora.data.local.entity.SupplyFormType.POWDER -> listOf("WP", "SP", "Other")
                                    com.mail2dev.planfora.data.local.entity.SupplyFormType.GRANULAR -> listOf("WG", "GR", "Other")
                                    com.mail2dev.planfora.data.local.entity.SupplyFormType.SOLID -> listOf("Other")
                                }
                                availableCodes.forEach { code ->
                                    DropdownMenuItem(text = { Text(code) }, onClick = { viewModel.updateFormulationCode(code); showFormulationPicker = false })
                                }
                            }
                        }
                    }
                }
            }

            // Permanent Notes
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Instructions / Notes") },
                placeholder = { Text("e.g. 1 capful = 10mL, Store in cool dark place") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = textFieldColors(),
                maxLines = 3
            )

            MediaAttachmentStrip(
                imageUris = imageUris,
                audioPath = viewModel.audioPath.collectAsState().value,
                onImagesAdd = { uris -> uris.forEach { viewModel.addImageUri(it) } },
                onImageRemove = { viewModel.removeImageUri(it) },
                onAudioCaptured = { viewModel.setAudioPath(it) },
                onAudioRemove = { viewModel.setAudioPath(null) }
            )

            // Optional Field Palette & Custom Field Creator
            OptionalSupplyPalette(
                category = category,
                visibleFields = visibleOptionalFields,
                onToggleField = viewModel::toggleOptionalField,
                onAddCustomField = { showCustomFieldDialog = true }
            )

            // Dynamic Custom Fields Renderer
            DynamicSupplyFieldRenderer(viewModel, visibleOptionalFields, customFieldDefinitions, customFieldValues)

            Button(
                onClick = {
                    viewModel.saveSupply()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (editingSupplyId == null) "Create Product Profile" else "Update Product Profile",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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

    if (showLocationSheet) {
        val masterLocations by viewModel.masterLocations.collectAsState()
        LocationSelectionBottomSheet(
            title = "📍 Storage Location",
            selectedLocation = location,
            masterLocations = masterLocations,
            onLocationSelected = viewModel::updateLocation,
            onNewLocationCreated = viewModel::addMasterLocation,
            onRenameLocation = viewModel::updateMasterLocation,
            onDeleteLocation = viewModel::deleteMasterLocation,
            onDismiss = { showLocationSheet = false }
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
fun ReadonlyTriggerField(label: String, value: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.clickable { onClick() },
        leadingIcon = { Icon(icon, null, tint = SageGreen, modifier = Modifier.size(18.dp)) },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.White,
            disabledBorderColor = Color.Gray.copy(alpha = 0.3f),
            disabledLabelColor = SageGreen
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionalSupplyPalette(
    category: SupplyCategory,
    visibleFields: Set<OptionalSupplyField>,
    onToggleField: (OptionalSupplyField) -> Unit,
    onAddCustomField: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Quick-Add Metrics & Metadata", style = MaterialTheme.typography.labelLarge, color = SageGreen, fontWeight = FontWeight.SemiBold)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = when (category) {
                SupplyCategory.FERTILIZER -> listOf(OptionalSupplyField.NPK, OptionalSupplyField.DILUTION)
                SupplyCategory.OTHER -> emptyList()
                else -> listOf(OptionalSupplyField.TARGET_PESTS, OptionalSupplyField.REI, OptionalSupplyField.DILUTION)
            }

            presets.forEach { field ->
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

@Composable
fun DynamicSupplyFieldRenderer(
    viewModel: AddSupplyViewModel,
    visibleFields: Set<OptionalSupplyField>,
    customFields: List<com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity>,
    customValues: Map<Long, String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        visibleFields.forEach { field ->
            MementoLedgerRow(
                label = field.displayName,
                onRemove = { viewModel.toggleOptionalField(field) }
            ) {
                SimpleTextFieldCompact("") {}
            }
        }

        customFields.forEach { def ->
            MementoLedgerRow(
                label = def.fieldName,
                onRemove = { /* Logic to hide */ }
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
fun RowScope.SimpleTextFieldCompact(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().weight(1.5f),
        placeholder = { Text("Enter...", fontSize = 12.sp) },
        singleLine = true,
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
fun CustomFieldCreatorDialog(onDismiss: () -> Unit, onFieldCreated: (String, String, String?, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("TEXT") }
    var options by remember { mutableStateOf("") }
    var isGlobal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2120),
        title = { Text("New Product Metadata Field", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Field Name") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isGlobal, onCheckedChange = { isGlobal = it })
                    Text("Global (All products)", color = Color.LightGray, fontSize = 12.sp)
                }
                Text("Input Type", color = Color.Gray, fontSize = 12.sp)
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

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SageGreen,
    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
    focusedLabelColor = SageGreen
)
