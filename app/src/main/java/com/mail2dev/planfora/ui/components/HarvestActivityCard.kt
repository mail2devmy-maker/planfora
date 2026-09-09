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
import com.mail2dev.planfora.ui.theme.*

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
    onGradeChange: (String) -> Unit,
    isImportant: Boolean = true
) {
    PlanForaSurfaceCard(title = "Granular Harvest Yield", isImportant = isImportant) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var showUnitPicker by remember { mutableStateOf(false) }
            Box {
                AssistChip(
                    onClick = { showUnitPicker = true },
                    label = { Text(yieldUnit) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (isImportant) SlateTextPrimary else SlateTextSecondary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(0.5.dp, if (isImportant) MandatoryBorder else OptionalBorder)
                )
                DropdownMenu(expanded = showUnitPicker, onDismissRequest = { showUnitPicker = false }) {
                    listOf("kg", "g", "units", "baskets", "crates").forEach { u ->
                        DropdownMenuItem(text = { Text(u) }, onClick = { onUnitChange(u); showUnitPicker = false })
                    }
                }
            }
        }

        if (availableZones.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Select Harvested Zones / Rows", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableZones.forEach { zone ->
                        val isSelected = selectedHarvestZones.contains(zone)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleHarvestZone(zone) },
                            label = { Text(zone, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isImportant) MandatoryBorder else OptionalBorder,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
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
                colors = planForaTextFieldColors(isImportant = isImportant)
            )
        } else {
            val zonesToRender = if (selectedHarvestZones.isEmpty()) emptyList() else selectedHarvestZones.toList().sorted()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            colors = planForaTextFieldColors(isImportant = isImportant)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Harvest Grade / Quality:", color = Color.Gray, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("A", "B", "Culls").forEach { grade ->
                    FilterChip(
                        selected = qualityGrade == grade,
                        onClick = { onGradeChange(grade) },
                        label = { Text(grade) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = qualityGrade == grade,
                            borderColor = if (isImportant) MandatoryBorder else OptionalBorder,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
        
        // Auto-aggregation summary
        val total = rowYields.entries
            .filter { (zone, _) -> availableZones.isEmpty() || selectedHarvestZones.contains(zone) }
            .mapNotNull { it.value.toDoubleOrNull() }
            .sum()

        if (total > 0) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Text(
                    "Total Aggregated Yield: $total $yieldUnit",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
