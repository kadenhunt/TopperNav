package edu.wku.toppernav.viewmodel

// NavigationViewModel - handles all the GPS location tracking and distance/ETA calculations
// This is where the "Navigate" screen gets its data from
// Uses Android's LocationManager to get GPS + Network location updates
// Calculates distance, bearing (compass direction), and ETA to the destination

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import edu.wku.toppernav.core.AppConfig
import edu.wku.toppernav.util.GeoUtils
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

class NavigationViewModel(app: Application) : AndroidViewModel(app) {

    // NavState holds everything the UI needs to show navigation info
    // User location, destination, distance, bearing (compass direction), ETA, status messages
    data class NavState(
        val hasPermission: Boolean = false,           // Do we have location permission?
        val userLat: Double? = null,                  // User's current latitude
        val userLng: Double? = null,                  // User's current longitude
        val userAlt: Double? = null,                  // User's altitude (for floor detection)
        val provider: String? = null,                 // GPS or Network provider
        val accuracyMeters: Float? = null,            // How accurate the location is
        val destLat: Double? = null,                  // Destination latitude
        val destLng: Double? = null,                  // Destination longitude
        val destAlt: Double? = null,                  // Destination altitude
        val destFloor: Int? = null,                   // Destination floor number
        val distanceMeters: Double? = null,           // Distance to destination in meters
        val bearingDeg: Double? = null,               // Compass bearing to destination (0-360)
        val etaMinutes: Int? = null,                  // Estimated time to walk there
        val status: String = "",                      // Status message to show user
        val floorAdvice: String? = null,              // "Go upstairs" etc when close
        val onRoute: Boolean = true                   // Are they heading the right way?
    )

    private val _state = MutableStateFlow(NavState())
    val state: StateFlow<NavState> = _state.asStateFlow()

    private var csvLogger: CsvLogger? = null

    init {
        // If CSV logging is enabled, create a logger to track navigation data
        if (AppConfig.enableCsvLogging) {
            csvLogger = CsvLogger(app, "nav_ticks.csv", header = "timestamp_ms,lat,lng,distance_m,bearing_deg,eta_min")
        }
    }

    // Set the destination coordinates (called when user selects a room)
    fun setDestination(lat: Double?, lng: Double?, alt: Double?, floor: Int?) {
        _state.value = _state.value.copy(destLat = lat, destLng = lng, destAlt = alt, destFloor = floor)
        Log.d("NAV", "setDestination lat=$lat lng=$lng alt=$alt floor=$floor")
        recompute() // Recalculate distance/ETA right away
    }

    // Set whether we have location permission
    fun setPermission(granted: Boolean) {
        _state.value = _state.value.copy(hasPermission = granted)
        Log.d("NAV", "permission set: $granted")
        if (granted) startLocationUpdates() // Start tracking location if we have permission
    }

    // forceRefresh - manually trigger a GPS update
    // On the BLU S5 (no rotation sensors), this is how users can refresh the arrow direction
    // They tap the Recenter button and we request a fresh location update immediately
    @SuppressLint("MissingPermission")
    fun forceRefresh() {
        val ctx = getApplication<Application>()
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!_state.value.hasPermission) {
            Log.w("NAV", "forceRefresh called but no location permission")
            return
        }
        try {
            // Ask all available providers (GPS + Network) for a single immediate update
            val providers = lm.allProviders.filter { lm.isProviderEnabled(it) }
            providers.forEach { provider ->
                lm.requestSingleUpdate(provider, object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        Log.d("NAV", "forceRefresh received location from $provider lat=${loc.latitude} lng=${loc.longitude}")
                        _state.value = _state.value.copy(
                            userLat = loc.latitude,
                            userLng = loc.longitude,
                            userAlt = if (loc.hasAltitude()) loc.altitude else null,
                            provider = loc.provider,
                            accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null,
                            status = "Location refreshed (${loc.provider})"
                        )
                        recompute() // Update distance/bearing/ETA with new location
                    }
                    override fun onProviderEnabled(p: String) {}
                    override fun onProviderDisabled(p: String) {}
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(p: String?, status: Int, extras: android.os.Bundle?) {}
                }, Looper.getMainLooper())
            }
            Log.d("NAV", "forceRefresh requested from providers: ${providers.joinToString()}")
        } catch (se: SecurityException) {
            Log.w("NAV", "forceRefresh failed: missing permission")
        } catch (ex: Exception) {
            Log.w("NAV", "forceRefresh error: ${ex.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val ctx = getApplication<Application>()
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Optional: seed with last known location so UI shows something fast
        try {
            // Mock hook: allow quick demo indoors if configured
            if (AppConfig.mockLocationEnabled) {
                _state.value = _state.value.copy(
                    userLat = AppConfig.mockLat,
                    userLng = AppConfig.mockLng,
                    userAlt = null,
                    provider = "MOCK",
                    accuracyMeters = null,
                    status = "Mock location active"
                )
                Log.d("NAV", "Seeded mock location lat=${AppConfig.mockLat} lng=${AppConfig.mockLng}")
                recompute()
                return
            }

            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val pick = lastGps ?: lastNet
            if (pick != null) {
                _state.value = _state.value.copy(
                    userLat = pick.latitude,
                    userLng = pick.longitude,
                    userAlt = if (pick.hasAltitude()) pick.altitude else null,
                    provider = pick.provider,
                    accuracyMeters = if (pick.hasAccuracy()) pick.accuracy else null,
                    status = "Seeded from last known (${pick.provider})"
                )
                Log.d("NAV", "Seeded lastKnownLocation provider=${pick.provider} lat=${pick.latitude} lng=${pick.longitude} acc=${pick.accuracy}")
                recompute()
            }
        } catch (se: SecurityException) {
            Log.w("NAV", "Unable to seed last known location: ${se.message}")
        }

        val requestFlow = callbackFlow<Location> {
            val listener = LocationListener { loc ->
                trySend(loc).isSuccess
            }
            try {
                val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (gpsEnabled) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                    Log.d("NAV", "Listening to GPS_PROVIDER")
                }
                if (netEnabled) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                    Log.d("NAV", "Listening to NETWORK_PROVIDER")
                }
                if (!gpsEnabled && !netEnabled) {
                    Log.w("NAV", "No location provider enabled (GPS/Network)")
                    _state.value = _state.value.copy(status = "No location provider enabled — enable Location in system settings")
                } else {
                    val providers = listOfNotNull(
                        if (gpsEnabled) "GPS" else null,
                        if (netEnabled) "NETWORK" else null
                    ).joinToString(" & ")
                    _state.value = _state.value.copy(status = "Awaiting first fix ($providers)")
                }
            } catch (se: SecurityException) {
                Log.w("NAV", "Location permission missing: ${se.message}")
                _state.value = _state.value.copy(status = "Location permission missing")
            } catch (ex: Exception) {
                Log.w("NAV", "Error requesting location updates: ${ex.message}")
                _state.value = _state.value.copy(status = "Error requesting location updates")
            }

            awaitClose { try { lm.removeUpdates(listener) } catch (_: Exception) {} }
        }

        // Track whether we received a location update within the configured timeout
        val firstUpdateReceived = AtomicBoolean(false)

        viewModelScope.launch(Dispatchers.IO) {
            // timeout watcher: if no first update after gpsFixTimeoutSec, update status (user-visible)
            val timeoutMs = (AppConfig.gpsFixTimeoutSec * 1000L)
            delay(timeoutMs)
            if (!firstUpdateReceived.get()) {
                Log.w("NAV", "No location fix after ${AppConfig.gpsFixTimeoutSec}s")
                _state.value = _state.value.copy(status = "No GPS fix after ${AppConfig.gpsFixTimeoutSec}s — ensure Location is ON and set to High accuracy (Wi‑Fi may help).")
            }
        }

        viewModelScope.launch {
            requestFlow.collectLatest { loc ->
                firstUpdateReceived.set(true)
                _state.value = _state.value.copy(
                    userLat = loc.latitude,
                    userLng = loc.longitude,
                    userAlt = if (loc.hasAltitude()) loc.altitude else null,
                    provider = loc.provider,
                    accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null,
                    status = "Location received (${loc.provider})"
                )
                Log.d("NAV", "Location update provider=${loc.provider} lat=${loc.latitude} lng=${loc.longitude} acc=${if (loc.hasAccuracy()) loc.accuracy else "?"}")
                recompute()
            }
        }
    }

    private var lastRecalcLat: Double? = null
    private var lastRecalcLng: Double? = null

    private fun shouldRecompute(lat: Double, lng: Double, destLat: Double, destLng: Double): Boolean {
        // If user or destination changed significantly or near destination -> recompute
        val d = GeoUtils.distanceMeters(lat, lng, destLat, destLng)
        if (d <= AppConfig.navNearThresholdMeters) return true
        val prevLat = lastRecalcLat
        val prevLng = lastRecalcLng
        if (prevLat == null || prevLng == null) return true
        val moved = GeoUtils.distanceMeters(prevLat, prevLng, lat, lng)
        if (moved >= AppConfig.navRecalcMoveThresholdMeters) return true
        return false
    }

    private fun recompute() {
        val s = _state.value
        val lat1 = s.userLat
        val lng1 = s.userLng
        val lat2 = s.destLat
        val lng2 = s.destLng
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return

        if (!shouldRecompute(lat1, lng1, lat2, lng2)) return

        val d = GeoUtils.distanceMeters(lat1, lng1, lat2, lng2)
        val b = GeoUtils.bearingDegrees(lat1, lng1, lat2, lng2)
        val walkingMps = AppConfig.walkingSpeedMps
        val etaMin = kotlin.math.max(1, (d / walkingMps / 60.0).toInt())

        Log.d("NAV", "Recomputed d=${"%.1f".format(d)}m b=${"%.0f".format(b)} eta=${etaMin}m")

        // Floor/altitude advice when close to destination
        var advice: String? = null
        if (d <= AppConfig.navNearThresholdMeters && AppConfig.enableFloorAdvice) {
            val sb = StringBuilder()
            s.destFloor?.let { sb.append("Proceed to floor $it") }
            val uAlt = s.userAlt
            val dAlt = s.destAlt
            if (uAlt != null && dAlt != null) {
                val diff = dAlt - uAlt
                val step = 2.5
                when {
                    diff > step -> sb.append(if (sb.isNotEmpty()) " • " else "").append("Go upstairs")
                    diff < -step -> sb.append(if (sb.isNotEmpty()) " • " else "").append("Go downstairs")
                }
            }
            advice = if (sb.isNotEmpty()) sb.toString() else null
        }

        val outOfCampus = lat1 !in AppConfig.campusMinLat..AppConfig.campusMaxLat ||
                lng1 !in AppConfig.campusMinLng..AppConfig.campusMaxLng
        val campusStatus = if (outOfCampus) " (Outside Campus Bounds)" else ""

        // Simple onRoute heuristic: if distance increases versus last recompute by > threshold, mark false
        val prevDistance = s.distanceMeters
        val onRoute = if (prevDistance != null && d - prevDistance > AppConfig.navOffRouteThresholdMeters) false else true

        val providerNote = s.provider?.let { " via $it" } ?: ""
        val accNote = s.accuracyMeters?.let { " • acc=${"%.1f".format(it)}m" } ?: ""

        _state.value = s.copy(
            distanceMeters = d,
            bearingDeg = b,
            etaMinutes = etaMin,
            status = "${"%.0f".format(d)} m • ${GeoUtils.toCardinal(b)}$providerNote$accNote$campusStatus",
            floorAdvice = advice,
            onRoute = onRoute
        )
        csvLogger?.log(
            listOf(
                System.currentTimeMillis().toString(),
                lat1.toString(),
                lng1.toString(),
                d.toString(),
                b.toString(),
                etaMin.toString()
            )
        )
        lastRecalcLat = lat1
        lastRecalcLng = lng1
    }
}

private class CsvLogger(app: Application, fileName: String, header: String) {
    private val file = java.io.File(app.getExternalFilesDir(null), fileName)
    init {
        if (!file.exists()) file.writeText(header + "\n")
    }
    @Synchronized fun log(columns: List<String>) {
        file.appendText(columns.joinToString(",") + "\n")
    }
}
