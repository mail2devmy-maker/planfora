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
import com.mail2dev.planfora.data.local.entity.displayName
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
    location: String? = null,
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
            location = location,
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
    location: String? = null,
    supplies: List<com.mail2dev.planfora.data.local.entity.DiySupplyEntity>,
    use24Hour: Boolean, 
    onFollowUpClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    val resolvedLocation = remember(location, log.parameters) {
        if (!location.isNullOrBlank()) {
            location
        } else {
            val params = log.parameters.split("|").associate { 
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else "" to ""
            }
            val loc = params["location"]
            val subLoc = params["subLocation"]
            when {
                !loc.isNullOrBlank() && !subLoc.isNullOrBlank() -> "$loc [$subLoc]"
                !loc.isNullOrBlank() -> loc
                else -> null
            }
        }
    }

    val photoCount = remember(log.imageUris) { log.imageUris.split(",").filter { it.isNotBlank() }.size }
    val audioCount = remember(log.audioFilePath) { if (log.audioFilePath.isNullOrBlank()) 0 else 1 }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (log.displayId.isNotBlank()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.displayId,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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

                if (!resolvedLocation.isNullOrBlank()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "📍 $resolvedLocation",
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                if (log.batchGroupId != null) {
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
            ReiBadge(log)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            if (log.supplyId != null || !log.customInputName.isNullOrBlank()) {
                val supplyName = if (log.supplyId != null) {
                    supplies.find { it.id == log.supplyId }?.displayName ?: "Deleted Supply"
                } else {
                    log.customInputName
                }
                
                if (!supplyName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Product: $supplyName",
                            color = SageGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            LogParameterGrid(log = log)

            if (log.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.note,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

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

            if (log.ecValue != null || log.phValue != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    log.ecValue?.let { MetricBadge("EC", it.toString()) }
                    log.phValue?.let { MetricBadge("pH", it.toString()) }
                }
            }
            
            if (log.audioFilePath != null) {
                Spacer(modifier = Modifier.height(12.dp))
                InlineAudioPlayer(log.audioFilePath)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bottom Notification Pill Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photoCount > 0) {
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$photoCount",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (audioCount > 0) {
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$audioCount",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        if (log.activityType == "PRODUCTION" || log.activityType == "Production") {
                            onEditClick()
                        } else {
                            onFollowUpClick()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SageGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (log.activityType == "PRODUCTION" || log.activityType == "Production") "Log Update" else "Add Follow-up", 
                        color = SageGreen, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
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

    val photoCount = remember(log.imageUris) { log.imageUris.split(",").filter { it.isNotBlank() }.size }
    val audioCount = remember(log.audioFilePath) { if (log.audioFilePath.isNullOrBlank()) 0 else 1 }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2120).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp).clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.displayId.isNotBlank()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.displayId,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

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
            ReiBadge(log)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            if (log.supplyId != null || !log.customInputName.isNullOrBlank()) {
                val supplyName = if (log.supplyId != null) {
                    supplies.find { it.id == log.supplyId }?.displayName ?: "Deleted Supply"
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

            if (photoCount > 0 || audioCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photoCount > 0) {
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$photoCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (audioCount > 0) {
                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$audioCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactActivityThread(
    parentLog: JournalLogEntity,
    followUps: List<JournalLogEntity>,
    assetName: String,
    location: String? = null,
    use24Hour: Boolean,
    onDeleteLog: (JournalLogEntity) -> Unit,
    onEditLog: (JournalLogEntity) -> Unit,
    onClick: (JournalLogEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E2120), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        CompactLogItem(
            log = parentLog,
            assetName = assetName,
            location = location,
            use24Hour = use24Hour,
            onDeleteClick = { onDeleteLog(parentLog) },
            onEditClick = { onEditLog(parentLog) },
            onClick = { onClick(parentLog) },
            elevation = 0.dp,
            backgroundColor = Color.Transparent
        )

        followUps.forEach { childLog ->
            Row(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .height(IntrinsicSize.Min)
            ) {
                // Visual Connector for List View
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .fillMaxHeight()
                        .background(SageGreen.copy(alpha = 0.2f))
                )
                
                CompactLogItem(
                    log = childLog,
                    assetName = "↳ Follow-up",
                    location = null,
                    use24Hour = use24Hour,
                    onDeleteClick = { onDeleteLog(childLog) },
                    onEditClick = { onEditLog(childLog) },
                    onClick = { onClick(childLog) },
                    elevation = 0.dp,
                    backgroundColor = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun CompactLogItem(
    log: JournalLogEntity, 
    assetName: String, 
    location: String? = null,
    use24Hour: Boolean,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    elevation: androidx.compose.ui.unit.Dp = 0.dp,
    backgroundColor: Color = Color(0xFF1E2120)
) {
    var showMenu by remember { mutableStateOf(false) }

    val resolvedLocation = remember(location, log.parameters) {
        if (!location.isNullOrBlank()) {
            location
        } else {
            val params = log.parameters.split("|").associate { 
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else "" to ""
            }
            val loc = params["location"]
            val subLoc = params["subLocation"]
            when {
                !loc.isNullOrBlank() && !subLoc.isNullOrBlank() -> "$loc [$subLoc]"
                !loc.isNullOrBlank() -> loc
                else -> null
            }
        }
    }

    val photoCount = remember(log.imageUris) { log.imageUris.split(",").filter { it.isNotBlank() }.size }
    val audioCount = remember(log.audioFilePath) { if (log.audioFilePath.isNullOrBlank()) 0 else 1 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (log.displayId.isNotBlank()) {
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    text = log.displayId,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = log.title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = assetName, color = SageGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)

                if (!resolvedLocation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📍 $resolvedLocation",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }

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
                val reiExpiryStr = log.parameters.split("|").find { it.startsWith("rei_expiry:") }?.substringAfter("rei_expiry:")
                if (reiExpiryStr != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    val isExpired = System.currentTimeMillis() >= (reiExpiryStr.toLongOrNull() ?: 0L)
                    Text(
                        text = if (isExpired) "✅ REI" else "⚠️ REI",
                        color = if (isExpired) SageGreen else Color(0xFFFFB74D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Media Notification Pill Badges
        if (photoCount > 0 || audioCount > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                if (photoCount > 0) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$photoCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (audioCount > 0) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$audioCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
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
fun LogParameterGrid(log: JournalLogEntity, modifier: Modifier = Modifier) {
    if (log.parameters.isBlank()) return

    val rawParams = remember(log.parameters) {
        log.parameters.split("|").mapNotNull { param ->
            val parts = param.split(":")
            if (parts.size == 2 && parts[0].isNotBlank()) parts[0].trim() to parts[1].trim() else null
        }.toMap()
    }

    val metrics = remember(rawParams) {
        val list = mutableListOf<Pair<String, String>>()
        
        // 1. Applied / Used Quantity & Unit
        val userQty = rawParams["user_entered_qty"]
        val usedQty = rawParams["used_qty"]
        val usedUnit = rawParams["used_unit"] ?: rawParams["unit"] ?: ""
        
        if (!userQty.isNullOrBlank()) {
            val formattedQty = if (usedUnit.isNotBlank()) "$userQty $usedUnit" else userQty
            list.add("Applied Quantity" to formattedQty)
        } else if (!usedQty.isNullOrBlank()) {
            val formattedQty = if (usedUnit.isNotBlank()) "$usedQty $usedUnit" else usedQty
            list.add("Used Quantity" to formattedQty)
        }

        // 2. Dosage & Ratio
        val dosage = rawParams["dosage"]
        val ratio = rawParams["ratio"]
        if (!dosage.isNullOrBlank()) {
            val formattedDosage = if (!ratio.isNullOrBlank()) "$dosage $ratio" else dosage
            list.add("Dosage" to formattedDosage)
        }

        // 3. Application Method
        val method = rawParams["method"]
        if (!method.isNullOrBlank()) {
            list.add("Application Method" to method)
        }

        // 4. Weeding Method
        val weedingMethod = rawParams["weeding_method"]
        if (!weedingMethod.isNullOrBlank()) {
            list.add("Weeding Method" to weedingMethod)
        }

        // 5. Harvest Yield & Grade
        val yieldVal = rawParams["yield"]
        val unitVal = rawParams["unit"]
        if (!yieldVal.isNullOrBlank()) {
            val formattedYield = if (!unitVal.isNullOrBlank()) "$yieldVal $unitVal" else yieldVal
            list.add("Total Yield" to formattedYield)
        }
        val grade = rawParams["grade"]
        if (!grade.isNullOrBlank()) {
            list.add("Quality Grade" to "Grade $grade")
        }
        val yieldBreakdown = rawParams["yield_breakdown"]
        if (!yieldBreakdown.isNullOrBlank()) {
            val formattedBreakdown = yieldBreakdown.replace(";", ", ").replace("=", ": ")
            list.add("Yield Breakdown" to formattedBreakdown)
        }

        // 6. Substrate Mix
        val substrate = rawParams["substrate"]
        if (!substrate.isNullOrBlank()) {
            list.add("Substrate Mix" to substrate)
        }

        // 7. Pot Size / Vessel
        val potSize = rawParams["pot_size"]
        if (!potSize.isNullOrBlank()) {
            list.add("Container / Vessel" to potSize)
        }

        // 8. Pruning Type
        val pruningType = rawParams["pruning_type"]
        if (!pruningType.isNullOrBlank()) {
            list.add("Pruning Type" to pruningType)
        }

        // 9. Targeted Zones
        val targetedZones = rawParams["targeted_zones"]
        if (!targetedZones.isNullOrBlank()) {
            list.add("Targeted Zones" to targetedZones)
        }

        // 10. Tool Used
        val toolUsed = rawParams["tool_used"]
        if (!toolUsed.isNullOrBlank()) {
            list.add("Tool Used" to toolUsed)
        }

        // 11. Custom / Unhandled Keys
        val handledKeys = setOf(
            "dosage", "ratio", "method", "phi_expiry", "rei_expiry", 
            "targeted_zones", "used_qty", "used_unit", "user_entered_qty", 
            "tool_used", "yield", "yield_breakdown", "unit", "grade", 
            "substrate", "pot_size", "pruning_type", "weeding_method", 
            "location", "subLocation"
        )

        rawParams.forEach { (key, value) ->
            if (key !in handledKeys && value.isNotBlank()) {
                val formattedKey = key.replace("_", " ").split(" ")
                    .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
                list.add(formattedKey to value)
            }
        }

        list
    }

    if (metrics.isEmpty()) return

    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val rows = metrics.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { (label, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label.uppercase(),
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = value,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
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

@Composable
fun ReiBadge(log: JournalLogEntity) {
    val reiExpiryStr = log.parameters.split("|").find { it.startsWith("rei_expiry:") }?.substringAfter("rei_expiry:")
    val reiExpiry = reiExpiryStr?.toLongOrNull() ?: return
    
    val currentTime = System.currentTimeMillis()
    val isExpired = currentTime >= reiExpiry
    
    val (badgeColor, label) = if (!isExpired) {
        val remainingMillis = reiExpiry - currentTime
        val remainingHours = (remainingMillis / (60L * 60 * 1000)).coerceAtLeast(1)
        val remainingMins = ((remainingMillis % (60L * 60 * 1000)) / (60L * 1000))
        Color(0xFFFFB74D) to "⚠️ REI: $remainingHours h ${remainingMins}m Left"
    } else {
        SageGreen to "✅ REI Cleared"
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
