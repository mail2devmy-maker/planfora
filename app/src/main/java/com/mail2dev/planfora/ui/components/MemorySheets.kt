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
fun TagPickerSheet(
    title: String = "Select Tags",
    masterTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onNewTagCreated: (String) -> Unit,
    onRenameTag: (String, String) -> Unit = { _, _ -> },
    onDeleteTag: (String) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    
    var tagToManage by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }
    
    val filteredTags = remember(masterTags, searchQuery) {
        if (searchQuery.isBlank()) masterTags
        else masterTags.filter { it.contains(searchQuery, ignoreCase = true) }
    }
    
    val exactMatchExists = remember(masterTags, searchQuery) {
        masterTags.any { it.equals(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1C1B),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .imePadding()
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Unified Search & Creation Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search or type tag...", color = Color.Gray) },
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

            // Dynamic Creation Action
            if (searchQuery.isNotBlank() && !exactMatchExists) {
                InputChip(
                    selected = false,
                    onClick = {
                        val newTag = searchQuery.trim().removePrefix("#")
                        onNewTagCreated(newTag)
                        onTagToggle(newTag)
                        searchQuery = ""
                    },
                    label = { Text("Create \"$searchQuery\"") },
                    leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)) },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = SageGreen.copy(alpha = 0.1f),
                        labelColor = SageGreen
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        borderColor = SageGreen,
                        selectedBorderColor = SageGreen,
                        enabled = true,
                        selected = false
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Existing Tags FlowRow
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredTags.forEach { tag ->
                    val isSelected = selectedTags.contains(tag)
                    Surface(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTagToggle(tag)
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                tagToManage = tag
                                renameValue = tag
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SageGreen else Color.White.copy(alpha = 0.05f),
                        contentColor = if (isSelected) DarkBackground else Color.LightGray,
                        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            if (tagToManage != null) {
                AlertDialog(
                    onDismissRequest = { tagToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Manage Tag", color = Color.White) },
                    text = { Text("Actions for \"$tagToManage\"", color = Color.LightGray) },
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

            if (showRenameDialog && tagToManage != null) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false; tagToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Rename Tag", color = Color.White) },
                    text = {
                        OutlinedTextField(
                            value = renameValue,
                            onValueChange = { renameValue = it },
                            label = { Text("New Tag Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (renameValue.isNotBlank() && renameValue != tagToManage) {
                                    onRenameTag(tagToManage!!, renameValue.trim())
                                }
                                showRenameDialog = false
                                tagToManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                        ) { Text("Update", color = DarkBackground) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false; tagToManage = null }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            if (showDeleteDialog && tagToManage != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false; tagToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Delete Tag?", color = Color.White) },
                    text = { Text("Are you sure you want to remove \"$tagToManage\" from the master list? This will not remove it from existing entries.", color = Color.LightGray) },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteTag(tagToManage!!)
                                showDeleteDialog = false
                                tagToManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Delete", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false; tagToManage = null }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
