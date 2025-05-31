package com.alejandro.habitjourney.features.task.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alejandro.habitjourney.core.presentation.ui.theme.AcentoInformativo
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.alejandro.habitjourney.R
import com.alejandro.habitjourney.features.task.data.local.PermissionType


@Composable
fun AlarmPermissionDialog(
    missingPermissions: List<PermissionType>,
    onPermissionSelected: (PermissionType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AcentoInformativo,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.permissions_needed))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.permissions_reminder_description),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                missingPermissions.forEach { permission ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onPermissionSelected(permission) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (permission) {
                                    PermissionType.EXACT_ALARM -> Icons.Default.Schedule
                                    PermissionType.NOTIFICATION_PERMISSION,
                                    PermissionType.NOTIFICATION_SETTINGS -> Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = AcentoInformativo,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (permission) {
                                        PermissionType.EXACT_ALARM  -> stringResource(R.string.permission_exact_alarm)
                                        PermissionType.NOTIFICATION_PERMISSION -> stringResource(R.string.permission_notification)
                                        PermissionType.NOTIFICATION_SETTINGS -> stringResource(R.string.permission_notification_settings)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when (permission) {
                                        PermissionType.EXACT_ALARM -> stringResource(R.string.permission_exact_alarm_desc)
                                        PermissionType.NOTIFICATION_PERMISSION -> stringResource(R.string.permission_notification_desc)
                                        PermissionType.NOTIFICATION_SETTINGS -> stringResource(R.string.permission_notification_settings_desc)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}