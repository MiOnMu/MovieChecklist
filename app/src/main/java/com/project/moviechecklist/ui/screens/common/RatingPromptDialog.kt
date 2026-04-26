package com.project.moviechecklist.ui.screens.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RatingPromptDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var currentRating by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this Movie/Series") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("How would you rate it (1-5 stars)?")
                Spacer(modifier = Modifier.height(16.dp))
                StarRatingInput(
                    currentRating = currentRating,
                    onRatingChange = { rating -> currentRating = rating }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(if (currentRating == 0) null else currentRating) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onConfirm(null)
                onDismiss()
            }) {
                Text("Skip Rating")
            }
        }
    )
}
