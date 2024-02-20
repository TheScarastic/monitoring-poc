package me.abhishek.activitymonitoring.location

data class LocationModel(
    val longitude: Double,
    val latitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val verticalAccuracyMeters: Float,
    val meanSeaAltitude: Double,
    val meanSeaAccuracy: Float,
    val speedMetersPerSecond: Float,
    val speedAccuracyMetersPerSecond: Float,
    val bearingDegree: Float,
    val bearingDegreeAccuracy: Float,
    @field:JvmField
    val isMocked: Boolean
)
