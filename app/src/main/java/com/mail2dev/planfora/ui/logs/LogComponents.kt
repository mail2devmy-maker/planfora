package com.mail2dev.planfora.ui.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.ui.components.InlineAudioPlayer
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.ui.theme.ForestGreen
import com.mail2dev.planfora.ui.theme.SageGreen
import java.io.File

@Composable
fun ActivityThread(
    parentLog: JournalLogEntity,
    followUps: List<JournalLogEntity>,
    assetName: String,
    supplies: List<com.mail2dev.planfora.data.local.entity.DiySupplyEntity>,
    use24Hour: Boolean,
    onFollowUpClick: () -> Unit,
    onDeleteLog: (JournalLogEntity) -> Unit,
    onEditLog: (JournalLogEntity) -> Unit,
    onLogClick: (Long) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExpandedLogCard(
            log = parentLog,
            assetName = assetName,
            supplies = supplies,
            use24Hour = use24Hour,
            onFollowUpClick = onFollowUpClick,
            onDeleteClick = { onDeleteLog(parentLog) },
            onEditClick = { onEditLog(parentLog) },
            onClick = { onLogClick(parentLog.id) }
        )
        
        followUps.forEach { childLog ->
            Row(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .height(IntrinsicSize.Min)
            ) {
                // Visual Connector
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(SageGreen.copy(alpha = 0.3f))
                )
                
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    val daysElapsed = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(childLog.timestamp - parentLog.timestamp)
                    val timeDeltaText = if (daysElapsed > 0) "↳ $daysElapsed days later" else "↳ Later same day"
                    
                    Text(
                        text = timeDeltaText,
                        color = SageGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                    )
                    
                    FollowUpLogCard(
                        log = childLog,
                        supplies = supplies,
                        use24Hour = use24Hour,
                        onDeleteClick = { onDeleteLog(childLog) },
                        onEditClick = { onEditLog(childLog) },
                        onClick = { onLogClick(childLog.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandedLogCard(
    log: JournalLogEntity, 
    assetName: String, 
    supplies: List<com.mail2dev.planfora.data.local.entity.DiySupplyEntity>,
    use24Hour: Boolean, 
    onFollowUpClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = SageGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = assetName,
                        color = SageGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (log.batchGroupId != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.People, null, tint = Color.LightGray, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Batch Log",
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (log.activityType != "Observation") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = ForestGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.activityType,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = com.mail2dev.planfora.util.TimeFormatter.formatTime(log.timestamp, use24Hour),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color(0xFF1E2120))) {
                        DropdownMenuItem(text = { Text("Edit", color = Color.White) }, onClick = { showMenu = false; onEditClick() })
                        DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showMenu = false; onDeleteClick() })
                    }
                }
            }
            
            PhiBadge(log)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            if (log.supplyId != null || !log.customInputName.isNullOrBlank()) {
                val supplyName = if (log.supplyId != null) {
                    supplies.find { it.id == log.supplyId }?.batchCode ?: "Deleted Supply"
                } else {
                    log.customInputName
                }
                
                if (!supplyName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = supplyName,
                            color = SageGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = log.note, color = Color.LightGray, fontSize = 14.sp)

            if (log.imageUris.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(log.imageUris.split(",")) { path ->
                        AsyncImage(
                            model = File(path),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            if (log.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    log.tags.split(",").forEach { tag ->
                        Surface(
                            color = ForestGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = SageGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (log.parameters.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    log.parameters.split("|").forEach { param ->
                        val parts = param.split(":")
                        if (parts.size == 2 && parts[0] != "phi_expiry") {
                            MetricBadge(parts[0], parts[1])
                        }
                    }
                }
            }

            if (log.ecValue != null || log.phValue != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    log.ecValue?.let { MetricBadge("EC", it.toString()) }
                    log.phValue?.let { MetricBadge("pH", it.toString()) }
                }
            }
            
            if (log.photoPath != null || log.audioFilePath != null) {
                Spacer(modifier = Modifier.height(12.dp))
                if (log.audioFilePath != null) {
                    InlineAudioPlayer(log.audioFilePath)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (log.photoPath != null) Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
                    if (log.audioFilePath != null) Icon(Icons.Default.Mic, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onFollowUpClick,
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SageGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Update", color = SageGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FollowUpLogCard(
    log: JournalLogEntity, 
    supplies: List<com.mail2dev.planfora.data.local.entity.DiySupplyEntity>,
    use24Hour: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp).clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ForestGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = log.activityType,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = com.mail2dev.planfora.util.TimeFormatter.formatTime(log.timestamp, use24Hour),
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color(0xFF1E2120))) {
                        DropdownMenuItem(text = { Text("Edit", color = Color.White) }, onClick = { showMenu = false; onEditClick() })
                        DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showMenu = false; onDeleteClick() })
                    }
                }
            }
            PhiBadge(log)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            if (log.supplyId != null || !log.customInputName.isNullOrBlank()) {
                val supplyName = if (log.supplyId != null) {
                    supplies.find { it.id == log.supplyId }?.batchCode ?: "Deleted Supply"
                } else {
                    log.customInputName
                }
                
                if (!supplyName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = supplyName,
                            color = SageGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (log.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = log.note, color = Color.LightGray, fontSize = 12.sp)
            }
            
            if (log.audioFilePath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                InlineAudioPlayer(log.audioFilePath)
            }
        }
    }
}

@Composable
fun CompactLogItem(
    log: JournalLogEntity, 
    assetName: String, 
    use24Hour: Boolean,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E2120), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = log.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = assetName, color = SageGreen, fontSize = 10.sp)
                val phiExpiryStr = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")
                if (phiExpiryStr != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    val isExpired = System.currentTimeMillis() >= (phiExpiryStr.toLongOrNull() ?: 0L)
                    Text(
                        text = if (isExpired) "✅ PHI" else "⚠️ PHI",
                        color = if (isExpired) SageGreen else Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        if (log.photoPath != null) Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp).padding(horizontal = 4.dp))
        if (log.audioFilePath != null) Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp).padding(horizontal = 4.dp))
        
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = com.mail2dev.planfora.util.TimeFormatter.formatTime(log.timestamp, use24Hour),
            color = Color.Gray,
            fontSize = 12.sp
        )

        if (onDeleteClick != null || onEditClick != null) {
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(Color(0xFF1E2120))) {
                    if (onEditClick != null) DropdownMenuItem(text = { Text("Edit", color = Color.White) }, onClick = { showMenu = false; onEditClick() })
                    if (onDeleteClick != null) DropdownMenuItem(text = { Text("Delete", color = Color.Red) }, onClick = { showMenu = false; onDeleteClick() })
                }
            }
        }
    }
}

@Composable
fun MetricBadge(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PhiBadge(log: JournalLogEntity) {
    val phiExpiryStr = log.parameters.split("|").find { it.startsWith("phi_expiry:") }?.substringAfter("phi_expiry:")
    val phiExpiry = phiExpiryStr?.toLongOrNull() ?: return
    
    val currentTime = System.currentTimeMillis()
    val isExpired = currentTime >= phiExpiry
    
    val (badgeColor, label) = if (!isExpired) {
        val remainingMillis = phiExpiry - currentTime
        val remainingDays = (remainingMillis / (24L * 60 * 60 * 1000)).coerceAtLeast(1)
        Color(0xFFFFB74D) to "⚠️ PHI: $remainingDays Days Left"
    } else {
        SageGreen to "✅ PHI Cleared"
    }

    Surface(
        color = badgeColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = label,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
