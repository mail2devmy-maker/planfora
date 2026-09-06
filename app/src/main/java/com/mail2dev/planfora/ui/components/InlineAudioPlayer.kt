package com.mail2dev.planfora.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.planfora.ui.theme.ForestGreen
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun InlineAudioPlayer(filePath: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }

    DisposableEffect(filePath) {
        val player = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                duration = this.duration
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = player
        
        onDispose {
            player.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = mediaPlayer?.currentPosition ?: 0
            delay(500)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                        isPlaying = false
                    } else {
                        it.start()
                        isPlaying = true
                        it.setOnCompletionListener { 
                            isPlaying = false
                            currentPosition = 0
                        }
                    }
                }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = ForestGreen
            )
        }

        LinearProgressIndicator(
            progress = { if (duration > 0) currentPosition.toFloat() / duration else 0f },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = ForestGreen,
            trackColor = Color.DarkGray
        )

        Text(
            text = formatMillis(currentPosition) + " / " + formatMillis(duration),
            color = Color.LightGray,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

private fun formatMillis(millis: Int): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
