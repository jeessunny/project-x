package com.projectx.studyhelper.presentation.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projectx.studyhelper.domain.model.UpdateInfo
import com.projectx.studyhelper.domain.model.UpdateState

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onConfirmUpdate: (UpdateInfo) -> Unit,
    onInstallNow: (UpdateInfo) -> Unit
) {
    when (updateState) {
        is UpdateState.Available -> {
            val updateInfo = updateState.updateInfo
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "System Update",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Update Available (${updateInfo.versionName})",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "A new version of Project X is ready for installation.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (updateInfo.fileSize > 0) {
                            val sizeMb = String.format("%.2f MB", updateInfo.fileSize / (1024f * 1024f))
                            Text(
                                text = "Download Size: $sizeMb",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "What's New:",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = updateInfo.releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirmUpdate(updateInfo) }
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        Text("Update Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Later")
                    }
                }
            )
        }

        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = { /* Non-cancelable during active download */ },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloading Update",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Downloading Update...",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (updateState.progressPercent >= 0) {
                            LinearProgressIndicator(
                                progress = { updateState.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${updateState.progressPercent}%",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                val downloadedMb = updateState.downloadedBytes / (1024 * 1024)
                                val totalMb = updateState.totalBytes / (1024 * 1024)
                                Text(
                                    text = "$downloadedMb MB / $totalMb MB",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Text(
                                text = "Streaming package...",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }

        is UpdateState.ReadyToInstall -> {
            val updateInfo = updateState.updateInfo
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Ready to Install",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Ready to Install",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Version ${updateInfo.versionName} has downloaded successfully. Proceed with the installation to apply updates.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(onClick = { onInstallNow(updateInfo) }) {
                        Text("Install Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            )
        }

        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Update Failed") },
                text = { Text(updateState.message) },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            )
        }

        else -> Unit
    }
}
