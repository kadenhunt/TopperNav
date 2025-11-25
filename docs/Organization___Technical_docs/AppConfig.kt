package edu.wku.toppernav.core

/**
 * UML: Singleton Pattern (placeholder). Diagram reference: fig:singleton.
 * Purpose: central place for simple app-wide flags (future: units, theme, feature toggles).
 * Current scope kept tiny to avoid over-engineering.
 */
object AppConfig {
    // Example feature toggles (adjust as we implement):
    var enableFloorAdvice: Boolean = true
    var walkingSpeedMps: Double = 1.4 // used for ETA heuristics

    // Optional: set true to force a mock location (useful for demo indoors)
    var mockLocationEnabled: Boolean = false
    var mockLat: Double = 36.98596
    var mockLng: Double = -86.44990

    // Fallback: if no GPS fix after this many seconds and mock disabled, show guidance message.
    var gpsFixTimeoutSec: Int = 10
    // Orientation smoothing factor (0..1) for heading averaging.
    var headingSmoothingAlpha: Float = 0.15f

    // thresholds used by NavigationViewModel
    var navRecalcMoveThresholdMeters: Double = 5.0
    var navOffRouteThresholdMeters: Double = 10.0
    var navNearThresholdMeters: Double = 25.0

    // Performance logging and campus boundary configuration
    var enableCsvLogging: Boolean = true // toggled for performance logging demonstration
    // Rough WKU campus bounding box (approximate; future refinement with polygon)
    const val campusMinLat = 36.9820
    const val campusMaxLat = 36.9905
    const val campusMinLng = -86.4555
    const val campusMaxLng = -86.4380
}
