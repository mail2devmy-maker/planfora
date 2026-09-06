package com.mail2dev.planfora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.ui.theme.SageGreen
import com.mail2dev.planfora.ui.theme.DarkBackground

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HarvestActivityCard(
    availableZones: List<String>,
    selectedHarvestZones: Set<String>,
    rowYields: Map<String, String>, // Zone to Amount
    yieldUnit: String,
    qualityGrade: String,
    onToggleHarvestZone: (String) -> Unit,
    onRowYieldChange: (String, String) -> Unit,
    onUnitChange: (String) -> Unit,
    onGradeChange: (String) -> Unit
) {
    Surface(
        color = Color(0xFF1E2120).copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Granular Harvest Yield", style = MaterialTheme.typography.labelLarge, color = SageGreen, fontWeight = FontWeight.SemiBold)
                
                var showUnitPicker by remember { mutableStateOf(false) }
                Box {
                    AssistChip(
                        onClick = { showUnitPicker = true },
                        label = { Text(yieldUnit) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(labelColor = SageGreen)
                    )
                    DropdownMenu(expanded = showUnitPicker, onDismissRequest = { showUnitPicker = false }) {
                        listOf("kg", "g", "units", "baskets", "crates").forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { onUnitChange(u); showUnitPicker = false })
                        }
                    }
                }
            }

            if (availableZones.isNotEmpty()) {
                Text("Select Harvested Zones / Rows", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableZones.forEach { zone ->
                        val isSelected = selectedHarvestZones.contains(zone)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleHarvestZone(zone) },
                            label = { Text(zone, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreen,
                                selectedLabelColor = DarkBackground
                            )
                        )
                    }
                }
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            }

            if (availableZones.isEmpty()) {
                // Default fallback if no zones defined
                OutlinedTextField(
                    value = rowYields["Total"] ?: "",
                    onValueChange = { onRowYieldChange("Total", it) },
                    label = { Text("Total Yield") },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text(yieldUnit) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SageGreen
                    )
                )
            } else {
                val zonesToRender = if (selectedHarvestZones.isEmpty()) emptyList() else selectedHarvestZones.toList().sorted()
                zonesToRender.forEach { zone ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(zone, modifier = Modifier.weight(1f), color = Color.White, fontSize = 14.sp)
                        OutlinedTextField(
                            value = rowYields[zone] ?: "",
                            onValueChange = { onRowYieldChange(zone, it) },
                            modifier = Modifier.weight(1.5f),
                            placeholder = { Text("0.0") },
                            suffix = { Text(yieldUnit) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SageGreen
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Grade:", color = Color.Gray, fontSize = 12.sp)
                listOf("A", "B", "Culls").forEach { grade ->
                    FilterChip(
                        selected = qualityGrade == grade,
                        onClick = { onGradeChange(grade) },
                        label = { Text(grade) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreen,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
            
            // Auto-aggregation summary
            val total = rowYields.entries
                .filter { (zone, _) -> availableZones.isEmpty() || selectedHarvestZones.contains(zone) }
                .mapNotNull { it.value.toDoubleOrNull() }
                .sum()

            if (total > 0) {
                Surface(
                    color = SageGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Total Aggregated Yield: $total $yieldUnit",
                        modifier = Modifier.padding(8.dp),
                        color = SageGreen,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
