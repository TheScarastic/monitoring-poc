package me.abhishek.activitymonitoring.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import me.abhishek.activitymonitoring.utils.Constatnts

@SuppressLint("MissingPermission")
class LocationMonitoring(
    private val context: Context,
    private val locationManager: LocationManager,
    private val firestore: FirebaseFirestore
) : LocationListenerCompat {

    private val oldLocations = hashMapOf<String?, LocationModel>()

    init {
        registerLocationListener()
    }

    private fun registerLocationListener() {
        locationManager.allProviders.forEach {
            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                it,
                LocationRequestCompat.Builder(INTERVAL).apply {
                    setMinUpdateDistanceMeters(UPDATE_DISTANCE)
                    setQuality(LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY)
                }.build(),
                this,
                context.mainLooper
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        addLocationToFirestore(location)
    }

    private fun addLocationToFirestore(location: Location) {
        val provider = location.provider ?: return

        val locationModel = LocationModel(
            longitude = location.latitude,
            latitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            verticalAccuracyMeters = location.verticalAccuracyMeters,
            meanSeaAltitude = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                && location.hasMslAltitude()
            )
                location.mslAltitudeMeters else 0.0,
            meanSeaAccuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                && location.hasMslAltitudeAccuracy()
            )
                location.mslAltitudeAccuracyMeters else 0F,
            speedMetersPerSecond = if (location.hasSpeedAccuracy())
                location.speedAccuracyMetersPerSecond else 0F,
            speedAccuracyMetersPerSecond = if (location.hasSpeedAccuracy())
                location.speedAccuracyMetersPerSecond else 0F,
            bearingDegree = if (location.hasBearing())
                location.bearing else 0F,
            bearingDegreeAccuracy = if (location.hasBearingAccuracy())
                location.bearingAccuracyDegrees else 0F,
            isMocked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                location.isMock else location.isFromMockProvider
        )

        if (locationModel != oldLocations[provider]) {
            oldLocations[provider] = locationModel
            // Update to firestore
            val locationData = hashMapOf(location.time.toString() to locationModel)
            firestore.collection(Constatnts.LOCATION_COLLECTION)
                .document(location.provider!!)
                .set(locationData, SetOptions.merge())
                .addOnFailureListener {
                    Log.e(TAG, "Location Data upload failed $it")
                }
        }
    }

    companion object {
        private const val TAG = "LocationMonitoring"
        private const val INTERVAL = 1 * 1000L // 1 second
        private const val UPDATE_DISTANCE = 20f // 20 meters
    }

}