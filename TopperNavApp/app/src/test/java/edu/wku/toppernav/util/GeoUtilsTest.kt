package edu.wku.toppernav.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoUtilsTest {

    @Test
    fun distance_same_point_is_zero() {
        val lat = 36.98596
        val lon = -86.44990
        val d = GeoUtils.distanceMeters(lat, lon, lat, lon)
        assertEquals(0.0, d, 0.0001)
    }

    @Test
    fun distance_known_pair_short() {
        // A small known offset (~11m east at latitude ~37)
        val lat1 = 36.98596
        val lon1 = -86.44990
        val lat2 = 36.98596
        val lon2 = -86.44980
        val d = GeoUtils.distanceMeters(lat1, lon1, lat2, lon2)
        // Expect roughly 8-12 meters depending on latitude; allow tolerance
        assertEquals(true, d in 5.0..20.0)
    }

    @Test
    fun bearing_cardinal() {
        // Going roughly east
        val lat1 = 36.98596
        val lon1 = -86.45
        val lat2 = 36.98596
        val lon2 = -86.44
        val bearing = GeoUtils.bearingDegrees(lat1, lon1, lat2, lon2)
        // Bearing east is approx 90 degrees
        assertEquals(true, bearing in 70.0..110.0)
        val card = GeoUtils.toCardinal(bearing)
        // Should map to E or NE/E depending on rounding; assert it contains 'E'
        assertEquals(true, card.contains("E"))
    }
}

