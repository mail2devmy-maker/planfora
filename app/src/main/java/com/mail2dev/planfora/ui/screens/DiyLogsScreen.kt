package com.mail2dev.planfora.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.displayName
import com.mail2dev.planfora.ui.logs.LogsViewModel
import com.mail2dev.planfora.ui.theme.DarkBackground
import com.mail2dev.planfora.util.TimeFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyLogsScreen(
    navController: NavController,
    viewModel: LogsViewModel,
    profileViewModel: com.mail2dev.planfora.ui.profile.ProfileViewModel
) {
    val diyLogs by viewModel.diyLogs.collectAsState()
    val supplies by viewModel.supplies.collectAsState()
    val use24Hour by profileViewModel.use24HourFormat.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DIY Laboratory Logs", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        if (diyLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Science, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No laboratory logs found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp) // High-density feel
            ) {
                items(diyLogs) { log ->
                    val supply = supplies.find { it.id == log.supplyId }
                    DiyLogLedgerEntry(log, supply?.displayName ?: "Unknown", use24Hour)
                }
            }
        }
    }
}

@Composable
fun DiyLogLedgerEntry(log: JournalLogEntity, batchName: String, use24Hour: Boolean) {
    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(log.timestamp))
    val timeStr = TimeFormatter.formatTime(log.timestamp, use24Hour)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$dateStr | $timeStr",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.width(100.dp)
            )
            
            Text(
                text = batchName.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.width(100.dp)
            )

            Text(
                text = log.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (log.note.isNotBlank()) {
            Text(
                text = log.note,
                color = Color.LightGray.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 100.dp, top = 4.dp)
            )
        }

        // Display materials if added mid-process
        val materials = log.parameters.split("|").find { it.startsWith("new_materials:") }
        if (materials != null) {
            val matList = materials.substringAfter("new_materials:").split(";")
            Column(modifier = Modifier.padding(start = 100.dp, top = 4.dp)) {
                matList.forEach { mat ->
                    if (mat.isNotBlank()) {
                        Text(
                            text = "+ $mat",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
    }
}
