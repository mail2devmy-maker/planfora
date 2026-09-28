package com.mail2dev.planfora.ui.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mail2dev.planfora.ui.theme.ForestGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogBottomSheet(
    viewModel: LogsViewModel,
    onDismiss: () -> Unit
) {
    val assets by viewModel.assets.collectAsState()
    var selectedAssetId by remember { mutableStateOf<Long?>(null) }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var ecValue by remember { mutableStateOf("") }
    var phValue by remember { mutableStateOf("") }
    var showAssetPicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New Journal Log", style = MaterialTheme.typography.headlineSmall)

            // Asset Picker
            Box {
                OutlinedTextField(
                    value = assets.find { it.id == selectedAssetId }?.name ?: "Select Plant / Asset",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plant Asset") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showAssetPicker = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showAssetPicker,
                    onDismissRequest = { showAssetPicker = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    assets.forEach { asset ->
                        DropdownMenuItem(
                            text = { Text(asset.name) },
                            onClick = {
                                selectedAssetId = asset.id
                                showAssetPicker = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Log Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ecValue,
                    onValueChange = { ecValue = it },
                    label = { Text("EC Value") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = phValue,
                    onValueChange = { phValue = it },
                    label = { Text("pH Value") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { /* TODO: Photo capture */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Photo")
                }
                Button(
                    onClick = { /* TODO: Voice note */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice")
                }
            }

            Button(
                onClick = {
                    val assetId = selectedAssetId ?: return@Button
                    viewModel.addJournalLog(
                        assetId = assetId,
                        title = title,
                        note = note,
                        photoPath = null,
                        audioFilePath = null,
                        ecValue = ecValue.toDoubleOrNull(),
                        phValue = phValue.toDoubleOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = selectedAssetId != null && title.isNotBlank()
            ) {
                Text("Save Journal Log")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
