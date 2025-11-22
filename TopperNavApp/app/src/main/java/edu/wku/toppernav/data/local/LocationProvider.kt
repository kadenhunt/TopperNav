package edu.wku.toppernav.data.local

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    /**
     * Continuous stream of location updates. Implementations should try to honor device power and
     * accuracy settings. Values should be emitted on a background dispatcher.
     */
    fun locationFlow(): Flow<Location>

    /**
     * Last known location or null if none available.
     */
    suspend fun lastKnownLocation(): Location?
}

