package com.yourpackage.moviechecklist.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingInput(
    maxStars: Int = 5,
    currentRating: Int,
    onRatingChange: (Int) -> Unit,
    starSize: Int = 32,
    starColor: Color = MaterialTheme.colorScheme.primary //Color.Yellow
) {
    Row {
        for (i in 1..maxStars) {
            Icon(
                imageVector = if (i <= currentRating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Star $i",
                tint = if (i <= currentRating) starColor else Color.Gray,
                modifier = Modifier
                    .size(starSize.dp)
                    .clickable { onRatingChange(i) }
            )
        }
    }
}