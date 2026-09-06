package com.mail2dev.planfora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.ui.theme.SageGreen
import com.mail2dev.planfora.ui.theme.DarkBackground

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ParameterInputSection(
    parameters: Map<String, String>,
    masterParameters: List<String>,
    onParameterChange: (String, String) -> Unit,
    onParameterRemove: (String) -> Unit,
    onCustomAdd: (String) -> Unit,
    onRenameParameter: (String, String) -> Unit = { _, _ -> },
    onDeleteParameter: (String) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showCustomInput by remember { mutableStateOf(false) }
    var customKey by remember { mutableStateOf("") }
    
    var parameterToManage by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Quick-Add Metrics", style = MaterialTheme.typography.labelLarge, color = SageGreen, fontWeight = FontWeight.SemiBold)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            masterParameters.forEach { preset ->
                val isAdded = parameters.containsKey(preset)
                FilterChip(
                    selected = isAdded,
                    onClick = { 
                        if (!isAdded) onParameterChange(preset, "") 
                        else onParameterRemove(preset)
                    },
                    label = { Text("+ $preset", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SageGreen,
                        selectedLabelColor = DarkBackground,
                        labelColor = Color.Gray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isAdded,
                        borderColor = Color.Gray.copy(alpha = 0.3f),
                        selectedBorderColor = SageGreen
                    ),
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            if (!isAdded) onParameterChange(preset, "")
                            else onParameterRemove(preset)
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            parameterToManage = preset
                            renameValue = preset
                        }
                    )
                )
            }
            
            AssistChip(
                onClick = { showCustomInput = !showCustomInput },
                label = { Text("Custom", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(labelColor = SageGreen),
                border = BorderStroke(0.5.dp, SageGreen.copy(alpha = 0.5f))
            )
        }

        if (showCustomInput) {
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.5.dp, SageGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customKey,
                        onValueChange = { customKey = it },
                        placeholder = { Text("e.g. LUX, Humidity", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SageGreen
                        )
                    )
                    IconButton(
                        onClick = {
                            if (customKey.isNotBlank()) {
                                onCustomAdd(customKey.trim())
                                customKey = ""
                                showCustomInput = false
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = SageGreen)
                    }
                }
            }
        }

        if (parameters.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                parameters.forEach { (key, value) ->
                    ParameterLedgerRow(
                        key = key,
                        value = value,
                        onValueChange = { onParameterChange(key, it) },
                        onRemove = { onParameterRemove(key) }
                    )
                }
            }
        }
    }

    if (parameterToManage != null) {
        AlertDialog(
            onDismissRequest = { parameterToManage = null },
            containerColor = Color(0xFF1E2120),
            title = { Text("Manage Metric", color = Color.White) },
            text = { Text("Actions for \"$parameterToManage\"", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { showRenameDialog = true }) {
                    Text("Rename", color = SageGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text("Delete", color = Color.Red)
                }
            }
        )
    }

    if (showRenameDialog && parameterToManage != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false; parameterToManage = null },
            containerColor = Color(0xFF1E2120),
            title = { Text("Rename Metric", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("New Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameValue.isNotBlank() && renameValue != parameterToManage) {
                            onRenameParameter(parameterToManage!!, renameValue.trim())
                        }
                        showRenameDialog = false
                        parameterToManage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                ) { Text("Update", color = DarkBackground) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false; parameterToManage = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showDeleteDialog && parameterToManage != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; parameterToManage = null },
            containerColor = Color(0xFF1E2120),
            title = { Text("Delete Metric?", color = Color.White) },
            text = { Text("Are you sure you want to remove \"$parameterToManage\" from the master list? Existing logs will keep their current values but the metric shortcut will be removed.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteParameter(parameterToManage!!)
                        showDeleteDialog = false
                        parameterToManage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; parameterToManage = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ParameterLedgerRow(
    key: String,
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
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
                text = key,
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1.5f),
                placeholder = { Text("0.0", fontSize = 12.sp) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SageGreen,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}
