package edu.wku.toppernav.data.model

/**
 * Represents a campus building. Backend team can extend this with more
 * metadata (floor plan references, accessibility info, etc.).
 */
data class Building(
    val id: String,            // Stable unique id (e.g., "B01")
    val name: String,          // Human readable name (e.g., "Building A")
    val floors: Int,           // Number of floors (optional now)
    val latitude: Double?,     // Null until real coordinates added
    val longitude: Double?     // Null until real coordinates added
)
