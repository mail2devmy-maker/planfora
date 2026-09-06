package com.mail2dev.planfora.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.ui.profile.ProfileViewModel
import com.mail2dev.planfora.ui.profile.SubscriptionTier
import com.mail2dev.planfora.ui.profile.UnitSystem
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.DeepCharcoal
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val unitSystem by viewModel.unitSystem.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()
    val subscriptionTier by viewModel.subscriptionTier.collectAsState()
    val lastSync by viewModel.lastSyncTimestamp.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val json = viewModel.exportLocalBackup()
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { it.readText() }
                if (json != null) {
                    val success = viewModel.restoreLocalBackup(json)
                    if (success) {
                        Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Restore failed: Invalid file", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ProfileHeader()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LabInsightsCard(analytics)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (analytics.maturingSupplies.isNotEmpty()) {
            ActiveFermentsCard(analytics.maturingSupplies)
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (analytics.topTags.isNotEmpty()) {
            TopTagsCard(analytics.topTags)
            Spacer(modifier = Modifier.height(24.dp))
        }

        PremiumCard(subscriptionTier, onUpgrade = viewModel::purchaseProTier)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SyncSection(lastSync, isSyncing, onSync = viewModel::triggerGoogleDriveSync)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        BackupSection(
            onExport = { createDocumentLauncher.launch("planfora_backup_${System.currentTimeMillis()}.json") },
            onRestore = { openDocumentLauncher.launch(arrayOf("application/json")) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PreferencesSection(
            unitSystem = unitSystem, 
            onUnitChange = viewModel::setUnitSystem,
            use24HourFormat = use24HourFormat,
            onTimeFormatChange = viewModel::setUse24HourFormat
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DangerZone(onResetClick = { showResetDialog = true })
        
        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Database") },
            text = { Text("This will permanently delete all your local plant assets, logs, and DIY formulations. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDatabase()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LabInsightsCard(analytics: com.mail2dev.planfora.ui.profile.LabAnalytics) {
    SettingsGroup("Lab Insights") {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InsightMetric("Plants", analytics.totalPlants.toString())
                InsightMetric("Logs", analytics.totalLogs.toString())
                InsightMetric("Active DIY", analytics.activeFerments.toString())
            }
        }
    }
}

@Composable
fun InsightMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Text(label, color = SageGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActiveFermentsCard(supplies: List<com.mail2dev.planfora.data.local.entity.DiySupplyEntity>) {
    SettingsGroup("Active DIY Ferments") {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                supplies.take(3).forEach { supply ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(supply.batchCode, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(supply.name, color = Color.Gray, fontSize = 12.sp)
                        }
                        
                        val remaining = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(supply.targetMaturityDate - System.currentTimeMillis())
                        Surface(
                            color = Color(0xFFFFB74D).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Text(
                                text = "${remaining}d left",
                                color = Color(0xFFFFB74D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopTagsCard(tags: List<com.mail2dev.planfora.ui.profile.TagCount>) {
    SettingsGroup("Top Experiment Tags") {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val maxCount = tags.firstOrNull()?.count ?: 1
                tags.forEach { tagCount ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(tagCount.tag, color = Color.White, fontSize = 14.sp)
                            Text(tagCount.count.toString(), color = SageGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { tagCount.count.toFloat() / maxCount },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = ForestGreen,
                            trackColor = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(DeepCharcoal, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = SageGreen, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("PlanFora Researcher", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("v1.0.0-alpha", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun PremiumCard(tier: SubscriptionTier, onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ForestGreen.copy(alpha = 0.8f), DeepCharcoal)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Verdant Tech Pro", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    if (tier == SubscriptionTier.PRO) {
                        Surface(color = SageGreen, shape = RoundedCornerShape(16.dp)) {
                            Text("Active", color = DarkBackground, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                BenefitItem("Unlimited Media Logs")
                BenefitItem("Google Drive Cloud Backup")
                BenefitItem("Advanced Export Tools")
                
                Spacer(modifier = Modifier.height(20.dp))
                
                if (tier == SubscriptionTier.FREE) {
                    Button(
                        onClick = onUpgrade,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen, contentColor = DarkBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Upgrade to Pro — One-time Purchase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
    }
}

@Composable
fun SyncSection(lastSync: Long?, isSyncing: Boolean, onSync: () -> Unit) {
    SettingsGroup("Cloud & Backup") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepCharcoal, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Cloud, contentDescription = null, tint = SageGreen)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Google Drive Sync", color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    text = if (lastSync == null) "Status: Never synced" 
                           else "Last sync: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastSync))}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            if (isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SageGreen, strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onSync) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sync Now", tint = SageGreen)
                }
            }
        }
    }
}

@Composable
fun BackupSection(onExport: () -> Unit, onRestore: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onExport,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export JSON")
        }
        OutlinedButton(
            onClick = onRestore,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) // Replace with upload if possible
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restore Data")
        }
    }
}

@Composable
fun PreferencesSection(
    unitSystem: UnitSystem, 
    onUnitChange: (UnitSystem) -> Unit,
    use24HourFormat: Boolean,
    onTimeFormatChange: (Boolean) -> Unit
) {
    SettingsGroup("App Preferences") {
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepCharcoal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unit System", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Current: ${unitSystem.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = unitSystem == UnitSystem.METRIC,
                        onCheckedChange = { onUnitChange(if (it) UnitSystem.METRIC else UnitSystem.IMPERIAL) },
                        colors = SwitchDefaults.colors(checkedThumbColor = SageGreen, checkedTrackColor = ForestGreen)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Time Format", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (use24HourFormat) "Current: 24-Hour" else "Current: 12-Hour",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = use24HourFormat,
                        onCheckedChange = onTimeFormatChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = SageGreen, checkedTrackColor = ForestGreen)
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("100% Local-First Storage", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DangerZone(onResetClick: () -> Unit) {
    Column {
        Text("Danger Zone", color = Color.Red.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Button(
            onClick = onResetClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Red),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Reset Local Database")
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, color = SageGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp), fontSize = 14.sp)
        content()
    }
}
