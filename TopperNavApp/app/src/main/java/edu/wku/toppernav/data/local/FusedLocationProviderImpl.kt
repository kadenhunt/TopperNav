package edu.wku.toppernav.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Concrete implementation of [LocationProvider] using FusedLocationProviderClient.
 * Emits location updates roughly once per second with balanced power accuracy.
 * Mapping: Component Diagram (GPS Service) -> Navigation Engine (ViewModel) via Flow<Location>.
 */
class FusedLocationProviderImpl(
    private val context: Context,
    private val request: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        1000L /* interval ms */
    )
        .setMinUpdateIntervalMillis(800L)
        .setMinUpdateDistanceMeters(1f)
        .build()
) : LocationProvider {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun locationFlow(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it).isSuccess }
            }
        }
        client.requestLocationUpdates(request, callback, context.mainLooper)
        awaitClose { client.removeLocationUpdates(callback) }
    }

    @SuppressLint("MissingPermission")
    override suspend fun lastKnownLocation(): Location? = client.lastLocation.await()
}

