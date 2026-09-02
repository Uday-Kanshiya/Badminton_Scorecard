package com.badminton.scorecard.feature.player.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onAddPlayer: (name: String, nickname: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Player") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Name") },
                    isError = showError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (showError) {
                        { Text("Name cannot be empty") }
                    } else null
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        onAddPlayer(
                            name.trim(),
                            nickname.trim().takeIf { it.isNotBlank() }
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPlayerDialog(
    initialName: String,
    initialNickname: String?,
    onDismiss: () -> Unit,
    onUpdatePlayer: (name: String, nickname: String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var nickname by remember { mutableStateOf(initialNickname ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit Player") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Name") },
                    isError = showError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (showError) {
                        { Text("Name cannot be empty") }
                    } else null
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        onUpdatePlayer(
                            name.trim(),
                            nickname.trim().takeIf { it.isNotBlank() }
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeletePlayerDialog(
    playerName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Player?") },
        text = { Text(text = "Are you sure you want to delete \"$playerName\"? Their statistics and profile will be permanently removed.") },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
