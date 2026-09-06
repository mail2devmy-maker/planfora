package com.mail2dev.planfora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.SageGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun StringPickerSheet(
    title: String,
    selectedValue: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onItemCreated: (String) -> Unit,
    onItemRenamed: (String, String) -> Unit,
    onItemDeleted: (String) -> Unit,
    onDismiss: () -> Unit,
    placeholder: String = "Search or type...",
    addLabel: String = "Add New"
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    
    var itemToManage by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.contains(searchQuery, ignoreCase = true) }
    }
    
    val exactMatchExists = remember(items, searchQuery) {
        items.any { it.equals(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1C1B),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().imePadding()) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(placeholder, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null, tint = SageGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SageGreen,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (searchQuery.isNotBlank() && !exactMatchExists) {
                InputChip(
                    selected = false,
                    onClick = {
                        onItemCreated(searchQuery.trim())
                        searchQuery = ""
                    },
                    label = { Text("$addLabel \"$searchQuery\"") },
                    leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)) },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = SageGreen.copy(alpha = 0.1f),
                        labelColor = SageGreen
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        borderColor = SageGreen,
                        enabled = true,
                        selected = false
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredItems.forEach { item ->
                    val isSelected = item == selectedValue
                    Surface(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                onItemSelected(item)
                                onDismiss()
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                itemToManage = item
                                renameValue = item
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SageGreen else Color.White.copy(alpha = 0.05f),
                        contentColor = if (isSelected) DarkBackground else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            if (itemToManage != null) {
                AlertDialog(
                    onDismissRequest = { itemToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Manage Entry", color = Color.White) },
                    text = { Text("Actions for \"$itemToManage\"", color = Color.LightGray) },
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

            if (showRenameDialog && itemToManage != null) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false; itemToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Rename Entry", color = Color.White) },
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
                                if (renameValue.isNotBlank() && renameValue != itemToManage) {
                                    onItemRenamed(itemToManage!!, renameValue.trim())
                                }
                                showRenameDialog = false
                                itemToManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                        ) { Text("Update", color = DarkBackground) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false; itemToManage = null }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            if (showDeleteDialog && itemToManage != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false; itemToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Delete Entry?", color = Color.White) },
                    text = { Text("Are you sure you want to remove \"$itemToManage\" from the master list?", color = Color.LightGray) },
                    confirmButton = {
                        Button(
                            onClick = {
                                onItemDeleted(itemToManage!!)
                                showDeleteDialog = false
                                itemToManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Delete", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false; itemToManage = null }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
