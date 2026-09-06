package com.mail2dev.planfora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
fun LocationSelectionBottomSheet(
    title: String = "Select Location",
    selectedLocation: String,
    masterLocations: List<String>,
    onLocationSelected: (String) -> Unit,
    onNewLocationCreated: (String) -> Unit,
    onRenameLocation: (String, String) -> Unit,
    onDeleteLocation: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var newLocName by remember { mutableStateOf("") }
    var isAddingNew by remember { mutableStateOf(false) }
    
    var locationToManage by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1C1B),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().imePadding()) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (!isAddingNew) {
                Button(
                    onClick = { isAddingNew = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen.copy(alpha = 0.1f), contentColor = SageGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Location")
                }
            } else {
                OutlinedTextField(
                    value = newLocName,
                    onValueChange = { newLocName = it },
                    placeholder = { Text("Enter location name...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            if (newLocName.isNotBlank()) {
                                onNewLocationCreated(newLocName.trim())
                                newLocName = ""
                                isAddingNew = false
                            }
                        }) { Icon(Icons.Default.Check, contentDescription = null, tint = SageGreen) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                masterLocations.forEach { loc ->
                    val isSelected = loc == selectedLocation
                    Surface(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                onLocationSelected(loc)
                                onDismiss()
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                locationToManage = loc
                                renameValue = loc
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SageGreen else Color.White.copy(alpha = 0.05f),
                        contentColor = if (isSelected) DarkBackground else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = loc,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
            
            if (locationToManage != null) {
                AlertDialog(
                    onDismissRequest = { locationToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Manage Location", color = Color.White) },
                    text = { Text("Actions for \"$locationToManage\"", color = Color.LightGray) },
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

            if (showRenameDialog && locationToManage != null) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false; locationToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Rename Location", color = Color.White) },
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
                                if (renameValue.isNotBlank() && renameValue != locationToManage) {
                                    onRenameLocation(locationToManage!!, renameValue.trim())
                                }
                                showRenameDialog = false
                                locationToManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                        ) { Text("Update", color = DarkBackground) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false; locationToManage = null }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            if (showDeleteDialog && locationToManage != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false; locationToManage = null },
                    containerColor = Color(0xFF1E2120),
                    title = { Text("Delete Location?", color = Color.White) },
                    text = { Text("Are you sure you want to remove \"$locationToManage\" from the master list? Assets already assigned to this location will not be changed.", color = Color.LightGray) },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteLocation(locationToManage!!)
                                showDeleteDialog = false
                                locationToManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Delete", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false; locationToManage = null }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
