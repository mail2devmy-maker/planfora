package com.mail2dev.planfora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class LocationZoneOption(
    val location: String,
    val subLocation: String = ""
) {
    val displayName: String
        get() = if (subLocation.isNotBlank()) "$location [$subLocation]" else location
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LocationSelectionBottomSheet(
    title: String = "Select Location / Block",
    selectedLocation: String,
    selectedSubLocation: String = "",
    masterLocations: List<String>,
    assets: List<com.mail2dev.planfora.data.local.entity.PlantAssetEntity> = emptyList(),
    onLocationSelected: (String) -> Unit,
    onLocationAndZoneSelected: ((location: String, subLocation: String) -> Unit)? = null,
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
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().imePadding()) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (!isAddingNew) {
                OutlinedButton(
                    onClick = { isAddingNew = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Location")
                }
            } else {
                OutlinedTextField(
                    value = newLocName,
                    onValueChange = { newLocName = it },
                    placeholder = { Text("Enter location name...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            if (newLocName.isNotBlank()) {
                                onNewLocationCreated(newLocName.trim())
                                newLocName = ""
                                isAddingNew = false
                            }
                        }) { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val locationZoneOptions = remember(masterLocations, assets) {
                val options = mutableListOf<LocationZoneOption>()
                val uniqueLocs = masterLocations.map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase() }
                
                uniqueLocs.forEach { loc ->
                    options.add(LocationZoneOption(location = loc, subLocation = ""))
                    val zones = assets
                        .filter { it.locationNote.trim().equals(loc.trim(), ignoreCase = true) && it.subLocation.isNotBlank() }
                        .map { it.subLocation.trim().uppercase() }
                        .distinct()
                        .sorted()
                    zones.forEach { zone ->
                        options.add(LocationZoneOption(location = loc, subLocation = zone))
                    }
                }
                options
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(locationZoneOptions) { opt ->
                    val isSelected = opt.location.trim().equals(selectedLocation.trim(), ignoreCase = true) &&
                            opt.subLocation.trim().equals(selectedSubLocation.trim(), ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (onLocationAndZoneSelected != null) {
                                        onLocationAndZoneSelected(opt.location, opt.subLocation)
                                    } else {
                                        onLocationSelected(opt.location)
                                    }
                                    onDismiss()
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    locationToManage = opt.location
                                    renameValue = opt.location
                                }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = opt.location,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (opt.subLocation.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Text(
                                        text = "[${opt.subLocation}]",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
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
                            Text("Rename", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.medium
                        ) { Text("Update", color = MaterialTheme.colorScheme.onPrimary) }
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
