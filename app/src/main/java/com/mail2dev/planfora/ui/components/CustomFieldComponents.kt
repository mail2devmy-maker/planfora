package com.mail2dev.planfora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldType
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomFieldDialog(
    targetType: FieldTargetType,
    scope: String,
    onDismiss: () -> Unit,
    onSave: (CustomFieldDefinitionEntity) -> Unit
) {
    var fieldName by remember { mutableStateOf("") }
    var fieldType by remember { mutableStateOf(CustomFieldType.TEXT) }
    var optionsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Custom Field", color = Color.White) },
        containerColor = Color(0xFF1E2120),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text("Field Name (e.g. Soil pH, Batch #)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Text("Data Type", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomFieldType.entries.forEach { type ->
                        FilterChip(
                            selected = fieldType == type,
                            onClick = { fieldType = type },
                            label = { Text(type.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color.DarkGray
                            )
                        )
                    }
                }

                if (fieldType == CustomFieldType.SINGLE_SELECT || fieldType == CustomFieldType.MULTI_SELECT) {
                    OutlinedTextField(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        label = { Text("Options (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val options = if (optionsText.isNotBlank()) {
                        Json.encodeToString(optionsText.split(",").map { it.trim() }.filter { it.isNotBlank() })
                    } else null
                    
                    onSave(
                        CustomFieldDefinitionEntity(
                            targetType = targetType,
                            scope = scope,
                            fieldName = fieldName,
                            fieldType = fieldType,
                            optionsJson = options
                        )
                    )
                },
                enabled = fieldName.isNotBlank()
            ) {
                Text("Create Field")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun ManageFieldDialog(
    definition: CustomFieldDefinitionEntity,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onArchive: () -> Unit
) {
    var newName by remember { mutableStateOf(definition.fieldName) }
    var showRenameDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Field", color = Color.White) },
        containerColor = Color(0xFF1E2120),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Field: ${definition.fieldName}", color = Color.LightGray)
                Text("Type: ${definition.fieldType.name}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                
                Button(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rename Field")
                }

                Button(
                    onClick = { onArchive(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF442222))
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Archive Field (Soft Delete)", color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) }
        }
    )

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Field", color = Color.White) },
            containerColor = Color(0xFF1E2120),
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { onRename(newName); showRenameDialog = false; onDismiss() }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicCustomFieldInput(
    definition: CustomFieldDefinitionEntity,
    value: String,
    onValueChange: (String) -> Unit
) {
    when (definition.fieldType) {
        CustomFieldType.TEXT, CustomFieldType.NUMBER -> {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                keyboardOptions = if (definition.fieldType == CustomFieldType.NUMBER) 
                    KeyboardOptions(keyboardType = KeyboardType.Decimal) 
                else KeyboardOptions.Default,
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField: @Composable () -> Unit ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = value,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        placeholder = { 
                            Text(
                                text = "Value...",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 1
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = true,
                                isError = false,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                ),
                                shape = RoundedCornerShape(8.dp),
                                focusedBorderThickness = 1.dp,
                                unfocusedBorderThickness = 1.dp
                            )
                        }
                    )
                }
            )
        }
        CustomFieldType.BOOLEAN -> {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = value.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                    Text(
                        text = if (value.toBoolean()) "Yes" else "No", 
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        CustomFieldType.SINGLE_SELECT -> {
            val options = remember(definition.optionsJson) {
                try {
                    definition.optionsJson?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            var expanded by remember { mutableStateOf(false) }
            
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (value.isBlank()) "Select Option" else value)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { onValueChange(option); expanded = false }
                        )
                    }
                }
            }
        }
        CustomFieldType.MULTI_SELECT -> {
            val options = remember(definition.optionsJson) {
                try {
                    definition.optionsJson?.let { Json.decodeFromString<List<String>>(it) } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            val selectedOptions = value.split(",").filter { it.isNotBlank() }.toSet()
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedOptions.contains(option),
                            onCheckedChange = { checked ->
                                val newSet = if (checked) selectedOptions + option else selectedOptions - option
                                onValueChange(newSet.joinToString(","))
                            }
                        )
                        Text(option, color = Color.White)
                    }
                }
            }
        }
    }
}
