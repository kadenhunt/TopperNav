package edu.wku.toppernav.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationScreen(
    destination: String?,
    etaText: String,
    travelTimeText: String,
    steps: List<String>,
    statusLine: String? = null,
    bearingDeg: Float? = null,
    floorAdvice: String? = null,
    debug: Boolean = true,
    debugLines: List<String> = emptyList(),
    onOpenLocationSettings: (() -> Unit)? = null
) {
    val ctx = LocalContext.current
    Surface(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = destination?.ifBlank { "No destination selected" } ?: "No destination selected",
                style = MaterialTheme.typography.headlineSmall
            )

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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = if (bearingDeg != null) Modifier.rotate(bearingDeg) else Modifier
                    )
                }
            }

            val eta = if (etaText.isNotBlank()) etaText else "—"
            Text("ETA: $eta", style = MaterialTheme.typography.bodyLarge)
            Text("Travel time: $travelTimeText", style = MaterialTheme.typography.bodyLarge)
            if (!statusLine.isNullOrBlank()) {
                Text(statusLine, style = MaterialTheme.typography.bodyMedium)
            }
            if (!floorAdvice.isNullOrBlank()) {
                Text(floorAdvice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            if (debug && debugLines.isNotEmpty()) {
                Text("Debug", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                debugLines.forEach { l ->
                    Text(l, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Row {
                    Button(onClick = { onOpenLocationSettings?.invoke() ?: ctx.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }) {
                        Text("Location Settings")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:" + ctx.packageName)
                        }
                        ctx.startActivity(intent)
                    }) {
                        Text("App Settings")
                    }
                }
            }

            Text("Steps :", style = MaterialTheme.typography.titleMedium)
            steps.forEach { s ->
                Text("• $s", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
