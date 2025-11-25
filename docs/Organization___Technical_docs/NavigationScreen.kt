package edu.wku.toppernav.ui.screens

// NavigationScreen - shows the arrow pointing to the destination, ETA, distance, etc.
// This is the main navigation UI that users see when they're trying to find a room
// Shows an arrow that points toward the destination and updates as they move

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import android.util.Log
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun NavigationScreen(
    destination: String?,
    etaText: String,                     // ETA to show ("8 min" etc)
    travelTimeText: String,              // Same as ETA for now
    steps: List<String>,                 // Turn-by-turn steps (not implemented yet)
    statusLine: String? = null,          // Status message to show under the arrow
    bearingDeg: Float? = null,           // Compass bearing to destination (0-360 degrees)
    floorAdvice: String? = null,         // "Go upstairs" etc when close to destination
    debug: Boolean = true,               // Show debug info at bottom of screen
    debugLines: List<String> = emptyList(), // Debug lines to display
    providerInfo: String? = null,        // Which location provider is active (GPS/Network)
    onOpenLocationSettings: (() -> Unit)? = null,
    onRecenter: (() -> Unit)? = null     // Callback for the Recenter button
) {
    val ctx = LocalContext.current

    // Try to get device heading from sensors (gyroscope/magnetometer)
    // BLU S5 doesn't have these, so deviceAzimuth stays null
    var deviceAzimuth by remember { mutableStateOf<Float?>(null) }
    var sensorAvailable by remember { mutableStateOf(true) }

    // Make "Location received" message fade out after 3 seconds
    val showReceived = remember { mutableStateOf(false) }
    LaunchedEffect(statusLine) {
        if (statusLine?.startsWith("Location received", ignoreCase = true) == true) {
            showReceived.value = true
            delay(3000) // Show for 3 seconds then fade out
            showReceived.value = false
        } else {
            showReceived.value = false
        }
    }

    // Try to register for device rotation sensors (for devices that have them)
    // On the BLU S5 this won't find any sensors, so arrow uses map bearing only
    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val rv = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // fallback storage for accelerometer + magnetic readings
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var haveGravity = false
        var haveGeomagnetic = false

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                try {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuthRad = orientation[0]
                        val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                        val normalized = (azimuthDeg + 360f) % 360f
                        deviceAzimuth = normalized
                        Log.d("NAV_SENSOR", "rotation_vector azimuth=$normalized")
                    } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        haveGravity = true
                        if (haveGeomagnetic) {
                            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                SensorManager.getOrientation(rotationMatrix, orientation)
                                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                                val normalized = (azimuthDeg + 360f) % 360f
                                deviceAzimuth = normalized
                                Log.d("NAV_SENSOR", "acc+mag azimuth=$normalized")
                            }
                        }
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        haveGeomagnetic = true
                        if (haveGravity) {
                            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                SensorManager.getOrientation(rotationMatrix, orientation)
                                val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                                val normalized = (azimuthDeg + 360f) % 360f
                                deviceAzimuth = normalized
                                Log.d("NAV_SENSOR", "acc+mag azimuth=$normalized")
                            }
                        }
                    }
                } catch (ex: Exception) {
                    Log.w("NAV_SENSOR", "sensor processing error: ${ex.message}")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Register available sensors. If rotation vector isn't available we'll register accel+mag.
        if (rv != null) {
            sm.registerListener(listener, rv, SensorManager.SENSOR_DELAY_UI)
        } else {
            // fallback to accelerometer + magnetic field
            val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (accel != null && mag != null) {
                sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
                sm.registerListener(listener, mag, SensorManager.SENSOR_DELAY_UI)
            } else {
                sensorAvailable = false
                Log.w("NAV_SENSOR", "No rotation-vector or accel+mag sensors available on device")
            }
        }

        onDispose {
            try {
                sm.unregisterListener(listener)
            } catch (_: Exception) {}
        }
    }

    // compute arrow rotation: we want the arrow to point to the destination on the screen
    // bearingDeg = degrees from north to destination; deviceAzimuth = degrees from north to device heading
    // arrowRotation = bearingDeg - deviceAzimuth (normalized)
    val arrowRotation = remember(bearingDeg, deviceAzimuth) {
        val da = deviceAzimuth
        val bd = bearingDeg
        if (bd == null || da == null) null
        else {
            var rot = (bd - da)
            rot = ((rot + 180f) % 360f + 360f) % 360f - 180f
            rot
        }
    }

    Surface(modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Reduced oval / container size so it doesn't dominate the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth(0.5f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // If we have a computed arrowRotation, rotate the icon by that value.
                    // Fallback: if device heading not available, rotate by absolute bearing (less accurate UX).
                    val mod = when {
                        arrowRotation != null -> Modifier.rotate(arrowRotation)
                        bearingDeg != null -> Modifier.rotate(bearingDeg)
                        else -> Modifier
                    }
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = "Compass arrow",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = mod
                    )
                }

                // Recenter button - forces immediate GPS refresh to update arrow direction
                // Useful workaround for devices without rotation sensors - user can tap frequently as they walk/turn
                if (onRecenter != null) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = { onRecenter() },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.MyLocation,
                            contentDescription = "Recenter - refresh location"
                        )
                    }
                }

                // Small status + spinner overlay at bottom of the oval
                val waiting = statusLine?.contains("Waiting", ignoreCase = true) == true || statusLine?.contains("Grant location", ignoreCase = true) == true || bearingDeg == null
                Row(modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (waiting) {
                        CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = statusLine ?: "Acquiring location...", style = MaterialTheme.typography.bodySmall)
                    } else {
                        // when statusLine indicates a fresh 'Location received' we show it briefly then fade out
                        if (statusLine?.startsWith("Location received", ignoreCase = true) == true) {
                            AnimatedVisibility(
                                visible = showReceived.value,
                                enter = fadeIn(animationSpec = tween(300)),
                                exit = fadeOut(animationSpec = tween(600))
                            ) {
                                Text(text = statusLine ?: "Location ready", style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            // persistent status (distance / cardinal direction)
                            Text(text = statusLine ?: "Location ready", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Always show ETA prominently so it doesn't disappear during demo or waiting states
            val eta = if (etaText.isNotBlank()) etaText else "—"
            Text("ETA: $eta", style = MaterialTheme.typography.bodyLarge)
            Text("Travel time: $travelTimeText", style = MaterialTheme.typography.bodyLarge)
            if (!floorAdvice.isNullOrBlank()) {
                Text(floorAdvice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            // Show provider / accuracy so user knows if GPS/Network/mock is in use
            if (!providerInfo.isNullOrBlank()) {
                Text("Source: $providerInfo", style = MaterialTheme.typography.bodySmall)
            }

            if (!statusLine.isNullOrBlank() && !statusLine.contains("Waiting", ignoreCase = true)) {
                // extra visual cue for being outside campus
                Text(statusLine, style = MaterialTheme.typography.bodyMedium)
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
