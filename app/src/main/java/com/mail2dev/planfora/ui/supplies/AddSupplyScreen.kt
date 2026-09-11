package com.mail2dev.planfora.ui.supplies

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.*
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
import com.mail2dev.planfora.data.local.entity.SupplyCategory
import com.mail2dev.planfora.ui.components.CreateCustomFieldDialog
import com.mail2dev.planfora.ui.components.DynamicCustomFieldInput
import com.mail2dev.planfora.ui.components.LocationSelectionBottomSheet
import com.mail2dev.planfora.ui.components.ManageFieldDialog
import com.mail2dev.planfora.ui.components.MediaAttachmentStrip
import com.mail2dev.planfora.ui.components.PlanForaFieldGroup
import com.mail2dev.planfora.ui.components.PlanForaSurfaceCard
import com.mail2dev.planfora.ui.components.StringPickerSheet
import com.mail2dev.planfora.ui.components.TagPickerSheet
import com.mail2dev.planfora.ui.components.planForaTextFieldColors
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import com.mail2dev.planfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AddSupplyScreen(
    viewModel: AddSupplyViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val category by viewModel.category.collectAsState()
    val subCategory by viewModel.subCategory.collectAsState()
    val materialLedger by viewModel.materialLedger.collectAsState()
    val formType by viewModel.formType.collectAsState()
    val formulationCode by viewModel.formulationCode.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val location by viewModel.location.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val imageUris by viewModel.imageUris.collectAsState()
    val customFieldDefinitions by viewModel.customFieldDefinitions.collectAsState()
    val customFieldValues by viewModel.customFieldValues.collectAsState()
    val editingSupplyId by viewModel.editingSupplyId.collectAsState()
    val isLabMode by viewModel.isLabMode.collectAsState()
    val isProductionUpdate by viewModel.isProductionUpdate.collectAsState()
    val historicalMaterialCount by viewModel.historicalMaterialCount.collectAsState()
    val customTimestamp by viewModel.customTimestamp.collectAsState()

    val context = LocalContext.current
    var showTagSheet by remember { mutableStateOf(false) }
    var showCustomFieldDialog by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val diyTypes = listOf("FPJ", "FFJ", "JMS", "JWA", "FAA", "OHN", "LAB", "WCA", "Other")

    val hasUnsavedChanges = remember(name, formulationCode, notes, location, selectedTags, imageUris, materialLedger) {
        name.isNotBlank() || formulationCode != null || notes.isNotBlank() || location.isNotBlank() || selectedTags.isNotEmpty() || imageUris.isNotEmpty() || materialLedger.isNotEmpty()
    }

    BackHandler(enabled = hasUnsavedChanges && editingSupplyId == null) {
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

    if (showDatePicker) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = customTimestamp }
        DisposableEffect(Unit) {
            val datePicker = android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    cal.set(java.util.Calendar.YEAR, year)
                    cal.set(java.util.Calendar.MONTH, month)
                    cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                    viewModel.updateCustomTimestamp(cal.timeInMillis)
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
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = customTimestamp }
        DisposableEffect(Unit) {
            val timePicker = android.app.TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    cal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                    cal.set(java.util.Calendar.MINUTE, minute)
                    viewModel.updateCustomTimestamp(cal.timeInMillis)
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

    ModalBottomSheet(
        onDismissRequest = {
            if (hasUnsavedChanges && editingSupplyId == null) {
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
                    when {
                        isProductionUpdate -> "Update Progress: $name"
                        editingSupplyId == null -> "New Product / Supply"
                        else -> "Edit Product / Supply"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    if (hasUnsavedChanges && editingSupplyId == null) {
                        showDiscardDialog = true
                    } else {
                        onDismiss()
                    }
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Date & Time Picker (Mandatory for DIY Lab updates/creations)
            if (isLabMode) {
                val dateText = remember(customTimestamp) {
                    java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(customTimestamp))
                }
                val timeText = remember(customTimestamp) {
                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(customTimestamp))
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
            }

            // Core Name Field
            if (!isProductionUpdate) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Product Name",
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
            }

            // Primary Identification & Storage
            PlanForaSurfaceCard(title = "Identification & Storage", isImportant = true) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isProductionUpdate && (!isLabMode || editingSupplyId != null)) {
                        Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SupplyCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { viewModel.updateCategory(cat) },
                                    label = { Text(cat.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = category == cat,
                                        borderColor = Color.Gray.copy(alpha = 0.2f),
                                        selectedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    // Location & Tags (Globally moved up for all categories, but for DIY it will be below Specification)
                    if (category != SupplyCategory.DIY) {
                        PlanForaFieldGroup(isImportant = true) {
                            ReadonlyTriggerField(
                                label = "Location",
                                value = location.ifBlank { "Select" },
                                icon = Icons.Default.LocationOn,
                                onClick = { showLocationSheet = true },
                                modifier = Modifier.weight(1f),
                                isImportant = true
                            )
                            if (!isProductionUpdate) {
                                ReadonlyTriggerField(
                                    label = "Tags",
                                    value = if (selectedTags.isEmpty()) "Select" else "${selectedTags.size} tags",
                                    icon = Icons.Default.Tag,
                                    onClick = { showTagSheet = true },
                                    modifier = Modifier.weight(1f),
                                    isImportant = true
                                )
                            }
                        }
                    }

                    if (category == SupplyCategory.DIY) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!isProductionUpdate && isLabMode) {
                                Text("DIY Specification", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    diyTypes.forEach { type ->
                                        FilterChip(
                                            selected = subCategory == type,
                                            onClick = { viewModel.updateSubCategory(type) },
                                            label = { Text(type, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = subCategory == type,
                                                borderColor = Color.Gray.copy(alpha = 0.2f),
                                                selectedBorderColor = Color.Transparent
                                            )
                                        )
                                    }
                                }
                            }

                            // Location & Vessel ID (Moved next to each other as requested)
                            PlanForaFieldGroup(isImportant = true) {
                                ReadonlyTriggerField(
                                    label = "Location",
                                    value = location.ifBlank { "Select" },
                                    icon = Icons.Default.LocationOn,
                                    onClick = { showLocationSheet = true },
                                    modifier = Modifier.weight(1f),
                                    isImportant = true
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Vessel / Jar ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateTextSecondary.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                    OutlinedTextField(
                                        value = viewModel.containerId.collectAsState().value,
                                        onValueChange = viewModel::updateContainerId,
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        placeholder = { Text("e.g. Jar A-01", fontSize = 12.sp) },
                                        colors = textFieldColors(isImportant = false),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                                    )
                                }
                            }

                            if (isLabMode && !isProductionUpdate) {
                                // Maturity and Tags row
                                PlanForaFieldGroup(isImportant = false) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Maturity (Days)", style = MaterialTheme.typography.labelSmall, color = SlateTextSecondary.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = viewModel.maturityDays.collectAsState().value,
                                            onValueChange = viewModel::updateMaturityDays,
                                            modifier = Modifier.fillMaxWidth().height(52.dp),
                                            placeholder = { Text("e.g. 30", fontSize = 12.sp) },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                            colors = textFieldColors(isImportant = false),
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                                        )
                                    }
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


                            if (isLabMode) {
                                // Material Ledger (DIY Production Only)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("MATERIAL LIST", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        TextButton(onClick = { viewModel.addMaterialToLedger() }) {
                                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add Item", fontSize = 11.sp)
                                        }
                                    }

                                    materialLedger.forEachIndexed { index, pair ->
                                        val isHistorical = index < historicalMaterialCount
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isHistorical) Color.White.copy(alpha = 0.03f) else Color.Transparent
                                                )
                                                .padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = pair.first,
                                                onValueChange = { if (!isHistorical) viewModel.updateMaterialInLedger(index, it, pair.second) },
                                                modifier = Modifier.weight(2f),
                                                placeholder = { Text("Ingredient", fontSize = 12.sp) },
                                                readOnly = isHistorical,
                                                colors = textFieldColors(isImportant = false),
                                                singleLine = true,
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    color = if (isHistorical) Color.Gray else Color.White,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            OutlinedTextField(
                                                value = pair.second,
                                                onValueChange = { if (!isHistorical) viewModel.updateMaterialInLedger(index, pair.first, it) },
                                                modifier = Modifier.weight(1f),
                                                placeholder = { Text("Qty", fontSize = 12.sp) },
                                                readOnly = isHistorical,
                                                colors = textFieldColors(isImportant = false),
                                                singleLine = true,
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    color = if (isHistorical) Color.Gray else Color.White,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            if (!isHistorical) {
                                                IconButton(onClick = { viewModel.removeMaterialFromLedger(index) }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                            } else {
                                                Icon(
                                                    Icons.Default.Lock, 
                                                    null, 
                                                    tint = Color.Gray.copy(alpha = 0.3f), 
                                                    modifier = Modifier.size(14.dp).padding(4.dp)
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

            // High-Speed Safety/Stock Fields
            val isHardware = category == SupplyCategory.SUPPLIES_TOOLS

            if (!isProductionUpdate || isLabMode) {
                PlanForaSurfaceCard(title = "Stock & Safety", isImportant = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!isProductionUpdate && category != SupplyCategory.DIY && !isHardware) {
                            var showIngredientSheet by remember { mutableStateOf(false) }
                            val activeIngredientValue = viewModel.activeIngredient.collectAsState().value
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Active Ingredient",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextPrimary.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                                OutlinedTextField(
                                    value = activeIngredientValue,
                                    onValueChange = viewModel::updateActiveIngredient,
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Tag, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        IconButton(onClick = { showIngredientSheet = true }) {
                                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Select A.I.")
                                        }
                                    },
                                    colors = textFieldColors(isImportant = true),
                                    placeholder = { Text("azoxystrobin") }
                                )
                            }

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

                            PlanForaFieldGroup(isImportant = true) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "PHI (Days)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateTextPrimary.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                    OutlinedTextField(
                                        value = viewModel.phiDays.collectAsState().value,
                                        onValueChange = viewModel::updatePhiDays,
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                        colors = textFieldColors(isImportant = true)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "REI (Hours)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateTextPrimary.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                    OutlinedTextField(
                                        value = viewModel.reiHours.collectAsState().value,
                                        onValueChange = viewModel::updateReiHours,
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                        colors = textFieldColors(isImportant = true)
                                    )
                                }
                            }
                        }

                        PlanForaFieldGroup(isImportant = true) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isLabMode) "Current Volume" else "Stock Amount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextPrimary.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                                OutlinedTextField(
                                    value = viewModel.stockQuantity.collectAsState().value,
                                    onValueChange = viewModel::updateStockQuantity,
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    colors = textFieldColors(isImportant = true)
                                )
                            }
                            var showUnitPicker by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Unit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateTextPrimary.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                    OutlinedTextField(
                                        value = viewModel.stockUnit.collectAsState().value,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = { showUnitPicker = true }) {
                                                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        colors = textFieldColors(isImportant = true)
                                    )
                                }
                                DropdownMenu(expanded = showUnitPicker, onDismissRequest = { showUnitPicker = false }) {
                                    listOf("L", "mL", "kg", "g", "units", "bottles").forEach { unit ->
                                        DropdownMenuItem(text = { Text(unit) }, onClick = { viewModel.updateStockUnit(unit); showUnitPicker = false })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Product Formulation Row
            if (!isHardware && !isProductionUpdate) {
                PlanForaSurfaceCard(title = "Product Formulation", isImportant = false) {
                    PlanForaFieldGroup(isImportant = false) {
                        var showFormTypePicker by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Form / Type",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextSecondary.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                                OutlinedTextField(
                                    value = formType.displayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { showFormTypePicker = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    colors = textFieldColors(isImportant = false)
                                )
                            }
                            DropdownMenu(expanded = showFormTypePicker, onDismissRequest = { showFormTypePicker = false }) {
                                SupplyFormType.entries.forEach { type ->
                                    DropdownMenuItem(text = { Text(type.displayName) }, onClick = { viewModel.updateFormType(type); showFormTypePicker = false })
                                }
                            }
                        }

                        if (!isLabMode) {
                            var showFormulationPicker by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1f)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Form. Code",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateTextSecondary.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                    OutlinedTextField(
                                        value = formulationCode ?: "N/A",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = { showFormulationPicker = true }) {
                                                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        colors = textFieldColors(isImportant = false)
                                    )
                                }
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
            }

            // Permanent Notes
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isProductionUpdate) "Progress Notes (e.g. Smell, Bubbling, Mixing)" else "Instructions / Notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateTextSecondary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 2.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = viewModel::updateNotes,
                    placeholder = { Text(if (isProductionUpdate) "Stirred today, smell is sweet..." else "e.g. \"2mL/L or 5g/10L\"") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = textFieldColors(isImportant = false),
                    maxLines = 3
                )
            }

            PlanForaSurfaceCard(title = "Attachments", isImportant = false) {
                MediaAttachmentStrip(
                    imageUris = imageUris,
                    audioPath = viewModel.audioPath.collectAsState().value,
                    onImagesAdd = { uris -> uris.forEach { viewModel.addImageUri(it) } },
                    onImageRemove = { viewModel.removeImageUri(it) },
                    onAudioCaptured = { viewModel.setAudioPath(it) },
                    onAudioRemove = { viewModel.setAudioPath(null) }
                )
            }

            // Metrics Section (Unified Optional Fields)
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

                DynamicSupplyFieldRenderer(viewModel, customFieldDefinitions, customFieldValues)
            }

            Button(
                onClick = {
                    viewModel.saveSupply()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = name.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    when {
                        isProductionUpdate -> "Record Production Update"
                        editingSupplyId == null -> "Create Product Profile"
                        else -> "Update Product Profile"
                    },
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
        CreateCustomFieldDialog(
            targetType = FieldTargetType.SUPPLY_CATEGORY,
            scope = category.displayName,
            onDismiss = { showCustomFieldDialog = false },
            onSave = { definition ->
                viewModel.addCustomFieldDefinition(definition)
                showCustomFieldDialog = false
            }
        )
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
                    color = if (value == "Select") Color.Gray else Color.White,
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
fun DynamicSupplyFieldRenderer(
    viewModel: AddSupplyViewModel,
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
fun textFieldColors(isImportant: Boolean = false) = planForaTextFieldColors(isImportant)
