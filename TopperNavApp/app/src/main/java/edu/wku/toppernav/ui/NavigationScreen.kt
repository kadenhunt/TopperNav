package edu.wku.toppernav.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun NavigationScreen(
    destination: String?,
    etaText: String,
    travelTimeText: String,
    steps: List<String>
) {
    Surface(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = destination?.ifBlank { "No destination selected" } ?: "No destination selected",
                style = MaterialTheme.typography.headlineSmall
            )

            // Compass placeholder: circle + upward navigation arrow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth(0.6f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = "Compass arrow",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text("ETA: $etaText", style = MaterialTheme.typography.bodyLarge)
            Text("Travel time: $travelTimeText", style = MaterialTheme.typography.bodyLarge)

            Text("Steps :", style = MaterialTheme.typography.titleMedium)
            steps.forEach { s ->
                Text("• $s", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
