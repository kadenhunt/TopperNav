package edu.wku.toppernav.viewmodel

// UML: State Pattern
// NavState models the dynamic state of navigation (permission, location, destination, metrics).
// Diagram reference: state_pattern (Behavioral Design Pattern: State).
// Component mapping: ViewModel layer in Component Diagram (UI -> ViewModel -> UseCase -> Repository -> DB).
// Sequence Diagram: Navigate screen triggers setDestination -> recompute -> emits NavState to UI.
// Security: Contains no sensitive data beyond coordinates; kept in-memory.

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import edu.wku.toppernav.core.AppConfig
import edu.wku.toppernav.util.GeoUtils
import android.util.Log

class NavigationViewModel(app: Application) : AndroidViewModel(app) {

    data class NavState(
        val hasPermission: Boolean = false,
        val userLat: Double? = null,
        val userLng: Double? = null,
        val userAlt: Double? = null,
        val destLat: Double? = null,
        val destLng: Double? = null,
        val destAlt: Double? = null,
        val destFloor: Int? = null,
        val distanceMeters: Double? = null,
        val bearingDeg: Double? = null,
        val etaMinutes: Int? = null,
        val status: String = "",
        val floorAdvice: String? = null
    )

    private val _state = MutableStateFlow(NavState())
    val state: StateFlow<NavState> = _state.asStateFlow()

    fun setDestination(lat: Double?, lng: Double?, alt: Double?, floor: Int?) {
        _state.value = _state.value.copy(destLat = lat, destLng = lng, destAlt = alt, destFloor = floor)
        recompute()
    }

    fun setPermission(granted: Boolean) {
        _state.value = _state.value.copy(hasPermission = granted)
        if (granted) startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val ctx = getApplication<Application>()
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Optional: seed with last known location so UI shows something fast
        try {
            // Mock hook: allow quick demo indoors if configured
            if (edu.wku.toppernav.core.AppConfig.mockLocationEnabled) {
                _state.value = _state.value.copy(
                    userLat = edu.wku.toppernav.core.AppConfig.mockLat,
                    userLng = edu.wku.toppernav.core.AppConfig.mockLng,
                    userAlt = null
                )
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
                    userAlt = if (pick.hasAltitude()) pick.altitude else null
                )
                Log.d("NAV", "Seeded lastKnownLocation lat=${pick.latitude} lng=${pick.longitude}")
                recompute()
            }
        } catch (_: SecurityException) {}

        val requestFlow = callbackFlow<Location> {
            val listener = LocationListener { loc ->
                trySend(loc).isSuccess
            }
            try {
                val requested = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                    true
                } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                    true
                } else false
                if (!requested) Log.w("NAV", "No location provider enabled (GPS/Network)")
            } catch (_: SecurityException) {}

            awaitClose { try { lm.removeUpdates(listener) } catch (_: Exception) {} }
        }

        viewModelScope.launch {
            requestFlow.collectLatest { loc ->
                _state.value = _state.value.copy(
                    userLat = loc.latitude,
                    userLng = loc.longitude,
                    userAlt = if (loc.hasAltitude()) loc.altitude else null
                )
                Log.d("NAV", "Location update lat=${loc.latitude} lng=${loc.longitude}")
                recompute()
            }
        }
    }

    private fun recompute() {
        val s = _state.value
        val lat1 = s.userLat
        val lng1 = s.userLng
        val lat2 = s.destLat
        val lng2 = s.destLng
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) return

        val d = GeoUtils.distanceMeters(lat1, lng1, lat2, lng2)
        val b = GeoUtils.bearingDegrees(lat1, lng1, lat2, lng2)
        val walkingMps = AppConfig.walkingSpeedMps
        val etaMin = kotlin.math.max(1, (d / walkingMps / 60.0).toInt())

        Log.d("NAV", "Recomputed d=${"%.1f".format(d)}m b=${"%.0f".format(b)} eta=${etaMin}m")

        // Floor/altitude advice when close to destination
        var advice: String? = null
        val nearThresholdM = 25.0
        if (d <= nearThresholdM && AppConfig.enableFloorAdvice) {
            val sb = StringBuilder()
            s.destFloor?.let { sb.append("Proceed to floor $it") }
            val uAlt = s.userAlt
            val dAlt = s.destAlt
            if (uAlt != null && dAlt != null) {
                val diff = dAlt - uAlt
                val step = 2.5 // meters ~ one floor difference heuristic
                when {
                    diff > step -> sb.append(if (sb.isNotEmpty()) " • " else "").append("Go upstairs")
                    diff < -step -> sb.append(if (sb.isNotEmpty()) " • " else "").append("Go downstairs")
                }
            }
            advice = if (sb.isNotEmpty()) sb.toString() else null
        }

        _state.value = s.copy(
            distanceMeters = d,
            bearingDeg = b,
            etaMinutes = etaMin,
            status = "${"%.0f".format(d)} m • ${GeoUtils.toCardinal(b)}",
            floorAdvice = advice
        )
    }
}
