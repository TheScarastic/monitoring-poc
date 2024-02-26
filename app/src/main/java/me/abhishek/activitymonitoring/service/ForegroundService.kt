package me.abhishek.activitymonitoring.service

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent

import android.content.pm.PackageManager

import android.hardware.SensorManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint
import me.abhishek.activitymonitoring.R
import me.abhishek.activitymonitoring.Traffic.TrafficMonitor
import me.abhishek.activitymonitoring.battery.BatteryMonitoring
import me.abhishek.activitymonitoring.location.LocationMonitoring
import me.abhishek.activitymonitoring.network.NetworkMonitor
import me.abhishek.activitymonitoring.sensors.SensorsMonitoring
import me.abhishek.activitymonitoring.usagestats.AppStatsMonitoring
import me.abhishek.activitymonitoring.utils.Constatnts

import javax.inject.Inject

/**
 * Foreground service to be replaced by background service
 * once the app is installed as system app, system puts restriction
 *
 */
@AndroidEntryPoint
class ForegroundService : Service() {

    @Inject
    lateinit var sensorManager: SensorManager

    @Inject
    lateinit var usageStatsManager: UsageStatsManager

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @Inject
    lateinit var subscriptionManager: SubscriptionManager

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create a notification
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setContentTitle(getString(R.string.foreground_service))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, notification)

        // Location Monitoring
        if (intent != null && intent.getBooleanExtra(Constatnts.LOCATION_EXTRA, false)) {
            LocationMonitoring(this, locationManager, firestore)
        }

        return START_REDELIVER_INTENT
    }


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "service created, location")

        UNIQUE_ID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        addDeviceToFirestore()

        // Sensors
        SensorsMonitoring(sensorManager, firestore)

        // Usage Stats
        if (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            AppStatsMonitoring(this, usageStatsManager, firestore)
        } else {
            Log.e(TAG, "permission missing for PACKAGE_USAGE_STATS")
        }

        // Battery
        BatteryMonitoring(this, firestore)

        // Network
        NetworkMonitor(this, connectivityManager,subscriptionManager, firestore)

        // Traffic
        TrafficMonitor(this, firestore).start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun addDeviceToFirestore() {
        val deviceName = Build.MODEL
        firestore.collection(Constatnts.DEVICE_ID_DOCUMENT).document(Constatnts.DEVICE_ID_DOCUMENT)
            .set(hashMapOf(UNIQUE_ID to deviceName), SetOptions.merge() )
    }

    companion object {
        const val NOTIFICATION_ID = 99
        const val TAG = "ForegroundService"
        lateinit var UNIQUE_ID: String
    }
}