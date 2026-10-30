package com.projectx.studyhelper.presentation.updater

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.projectx.studyhelper.domain.model.UpdateInfo
import com.projectx.studyhelper.domain.model.UpdateState

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onConfirmUpdate: (UpdateInfo) -> Unit,
    onInstallNow: () -> Unit
) {
    if (updateState is UpdateState.Idle) return

    val isDownloading = updateState is UpdateState.Downloading

    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        icon = {
            val icon = when (updateState) {
                is UpdateState.Downloading -> Icons.Default.CloudDownload
                is UpdateState.ReadyToInstall -> Icons.Default.InstallMobile
                is UpdateState.Error -> Icons.Default.ErrorOutline
                else -> Icons.Default.SystemUpdate
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            val titleText = when (updateState) {
                is UpdateState.Available -> "Update Available (${updateState.updateInfo.versionName})"
                is UpdateState.Downloading -> "Downloading Update"
                is UpdateState.ReadyToInstall -> "Download Complete"
                is UpdateState.Error -> "Update Failed"
                else -> "Project X Update"
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            AnimatedContent(
                targetState = updateState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "UpdateDialogContent"
            ) { state ->
                when (state) {
                    is UpdateState.Available -> {
                        val info = state.updateInfo
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "A new version of Project X is available with new features and improvements.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (info.fileSize > 0) {
                                val sizeMb = String.format("%.2f MB", info.fileSize / (1024f * 1024f))
                                Text(
                                    text = "Package size: $sizeMb",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Text(
                                text = "What's New:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = info.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    is UpdateState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (state.progressPercent >= 0 && state.downloadedBytes > 0) {
                                Text(
                                    text = "Downloading update package...",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { (state.progressPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${state.progressPercent}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val downloadedMb = state.downloadedBytes / (1024f * 1024f)
                                    val totalMb = state.totalBytes / (1024f * 1024f)
                                    Text(
                                        text = String.format("%.1f MB / %.1f MB", downloadedMb, totalMb),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = "Gathering package details & connecting to server...",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                )
                                Text(
                                    text = "Preparing secure download stream...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is UpdateState.ReadyToInstall -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Package verified and ready",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "The update package has downloaded successfully. Tap 'Install Now' to launch the Android package installer.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    is UpdateState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    else -> Unit
                }
            }
        },
        confirmButton = {
            when (updateState) {
                is UpdateState.Available -> {
                    Button(onClick = { onConfirmUpdate(updateState.updateInfo) }) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Now")
                    }
                }
                is UpdateState.ReadyToInstall -> {
                    Button(onClick = onInstallNow) {
                        Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Install Now")
                    }
                }
                is UpdateState.Error -> {
                    Button(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
                else -> Unit
            }
        },
        dismissButton = {
            if (updateState is UpdateState.Available) {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}
