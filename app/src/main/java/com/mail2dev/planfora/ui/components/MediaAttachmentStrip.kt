package com.mail2dev.planfora.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.mail2dev.planfora.ui.theme.ForestEmerald
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@Composable
fun MediaAttachmentStrip(
    imageUris: List<Uri>,
    audioPath: String?,
    onImagesAdd: (List<Uri>) -> Unit,
    onImageRemove: (Uri) -> Unit,
    onAudioCaptured: (String) -> Unit,
    onAudioRemove: () -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var tempAudioFile by remember { mutableStateOf<File?>(null) }
    val amplitudeList = remember { mutableStateListOf<Float>() }
    var showRedoConfirm by remember { mutableStateOf(false) }

    // Camera URI persistence
    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permission granted. Tap again to record.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Audio permission is required to record voice notes.", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        onImagesAdd(uris)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { onImagesAdd(listOf(it)) }
        }
        tempCameraUri = null
    }

    // Lifecycle cleanup
    DisposableEffect(Unit) {
        onDispose {
            recorder?.apply {
                try {
                    stop()
                } catch (_: Exception) {}
                release()
            }
            recorder = null
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            amplitudeList.clear()
            repeat(40) { amplitudeList.add(0.05f) }

            var ticker = 0
            while (isRecording) {
                val currentRecorder = recorder
                val raw = try {
                    currentRecorder?.maxAmplitude?.toFloat() ?: 0f
                } catch (e: Exception) {
                    0f
                }

                // Highly sensitive divisor (4000f) so normal speech creates immediate spikes,
                // while silence stays at a clean, low baseline instead of disappearing completely.
                val normalized = (raw / 4000f).coerceIn(0f, 1f)
                val visualValue = Math.sqrt(normalized.toDouble()).toFloat().coerceAtLeast(0.05f)

                if (amplitudeList.size >= 40) {
                    amplitudeList.removeAt(0)
                }
                amplitudeList.add(visualValue)

                ticker++
                if (ticker >= 20) {
                    recordingDuration++
                    ticker = 0
                }

                delay(50)
            }
        }
    }

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        try {
            val file = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")
            tempAudioFile = file
            
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = newRecorder
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to start recording: ${e.message}", Toast.LENGTH_SHORT).show()
            recorder?.release()
            recorder = null
        }
    }

    if (showRedoConfirm) {
        AlertDialog(
            onDismissRequest = { showRedoConfirm = false },
            containerColor = Color(0xFF1E2120),
            title = { Text("Replace existing voice note?", color = Color.White, fontSize = 18.sp) },
            text = { Text("Starting a new recording will overwrite your current voice note.", color = Color.Gray) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRedoConfirm = false
                        onAudioRemove()
                        startRecording()
                    }
                ) { Text("Replace / Redo", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRedoConfirm = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AnimatedContent(
            targetState = isRecording,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "MediaControls"
        ) { recording ->
            if (recording) {
                AudioRecordingHUD(
                    durationSeconds = recordingDuration,
                    amplitudes = amplitudeList.toList(), // Force recomposition on each poll
                    onStop = {
                        try {
                            recorder?.apply {
                                stop()
                                release()
                            }
                            recorder = null
                            isRecording = false
                            tempAudioFile?.let { onAudioCaptured(it.absolutePath) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isRecording = false
                            recorder = null
                        }
                    },
                    onCancel = {
                        try {
                            recorder?.apply {
                                stop()
                                release()
                            }
                            recorder = null
                            isRecording = false
                            tempAudioFile?.delete()
                            tempAudioFile = null
                        } catch (e: Exception) {
                            isRecording = false
                            recorder = null
                        }
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MediaActionButton(Icons.Default.CameraAlt, "Camera", Modifier.weight(1f)) { 
                        val file = File(context.filesDir, "Pictures").apply { if (!exists()) mkdirs() }
                        val imageFile = File(file, "cam_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "com.mail2dev.planfora.fileprovider",
                            imageFile
                        )
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                    MediaActionButton(Icons.Default.PhotoLibrary, "Gallery", Modifier.weight(1f)) {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    MediaActionButton(Icons.Default.Mic, "Audio", Modifier.weight(1f)) {
                        if (audioPath != null) {
                            showRedoConfirm = true
                        } else {
                            startRecording()
                        }
                    }
                }
            }
        }

        if (imageUris.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(imageUris) { uri ->
                    MediaPreviewItem(
                        uri = uri,
                        onRemove = { onImageRemove(uri) }
                    )
                }
            }
        }

        if (audioPath != null) {
            AudioPreviewCard(
                filePath = audioPath,
                onRemove = onAudioRemove
            )
        }
    }
}

@Composable
fun AudioRecordingHUD(
    durationSeconds: Int,
    amplitudes: List<Float>,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Surface(
        color = Color.Red.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = alpha))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatDuration(durationSeconds),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Amplitude Visualizer
            Box(modifier = Modifier.weight(1f).height(24.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = 2.dp.toPx()
                    val gap = 2.dp.toPx()
                    val centerY = size.height / 2
                    val displayList = if (amplitudes.isEmpty()) List(30) { 0.2f } else amplitudes

                    val totalWidth = displayList.size * (barWidth + gap)
                    val startX = (size.width - totalWidth).coerceAtLeast(0f)

                    displayList.forEachIndexed { index, amp ->
                        val barHeight = (amp * size.height * 0.95f).coerceIn(4.dp.toPx(), size.height)
                        val x = startX + index * (barWidth + gap)

                        if (x + barWidth <= size.width) {
                            drawRoundRect(
                                color = Color.Red.copy(alpha = 0.9f),
                                topLeft = Offset(x, centerY - barHeight / 2),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.LightGray)
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Save", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MediaActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(44.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
fun MediaPreviewItem(uri: Uri, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(72.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentScale = ContentScale.Crop
        )
        Surface(
            onClick = onRemove,
            color = Color.Black.copy(alpha = 0.7f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(18.dp)
        ) {
            Icon(
                Icons.Default.Close, 
                null, 
                tint = Color.White, 
                modifier = Modifier.padding(3.dp)
            )
        }
    }
}

@Composable
fun AudioPreviewCard(filePath: String, onRemove: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top Row (Header & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice Note", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
            
            // Bottom Row (Playback Controls)
            InlineAudioPlayer(filePath)
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
