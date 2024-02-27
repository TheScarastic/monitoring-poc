package me.abhishek.activitymonitoring.service

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Binder
import android.os.IBinder
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import me.abhishek.activitymonitoring.MainActivity
import me.abhishek.activitymonitoring.MainActivity.Companion.LOCATION_RESULT_CODE_BACKGROUND
import me.abhishek.activitymonitoring.MainActivity.Companion.PHONE_RESULT_STATE
import me.abhishek.activitymonitoring.R
import me.abhishek.activitymonitoring.ServiceCallback
import me.abhishek.activitymonitoring.battery.BatteryMonitoring
import me.abhishek.activitymonitoring.location.LocationMonitoring
import me.abhishek.activitymonitoring.network.NetworkMonitor
import me.abhishek.activitymonitoring.sensors.SensorsMonitoring
import me.abhishek.activitymonitoring.traffic.TrafficMonitor
import me.abhishek.activitymonitoring.usagestats.AppStatsMonitoring
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

    private var serviceCallback: ServiceCallback? = null
    private val binder = LocalBinder()
    var isRunning = false
        set(value) {
            field = value
            serviceCallback?.serviceLifecycleUpdated()
        }

    private var locationMonitoring: LocationMonitoring? = null
    private var sensorsMonitoring: SensorsMonitoring? = null
    private var appStatsMonitoring: AppStatsMonitoring? = null
    private var batteryMonitoring: BatteryMonitoring? = null
    private var networkMonitor: NetworkMonitor? = null
    private var trafficMonitor: TrafficMonitor? = null


    inner class LocalBinder : Binder() {
        fun getService(): ForegroundService {
            return this@ForegroundService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true

        // Create a notification
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setContentTitle(getString(R.string.foreground_service))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, notification)

        val permissionFlags = intent?.getIntExtra(MainActivity.PERMISSION_EXTRA, 0) ?: 0
        val hasLocationPermission =
            (permissionFlags and LOCATION_RESULT_CODE_BACKGROUND) == LOCATION_RESULT_CODE_BACKGROUND
        val hasPhoneStatePermission =
            (permissionFlags and PHONE_RESULT_STATE) == PHONE_RESULT_STATE


        // Location Monitoring
        if (hasLocationPermission) {
            locationMonitoring = LocationMonitoring(this, locationManager, firestore)
        } else {
            Log.e(TAG, "Skipping location monitoring, Permission missing")
        }

        // Sensors
        sensorsMonitoring = SensorsMonitoring(sensorManager, firestore)

        // Usage Stats
        if (MainActivity.checkUsageStatsPermission(this, applicationInfo)) {
            appStatsMonitoring = AppStatsMonitoring(this, usageStatsManager, firestore)
        } else {
            Log.e(TAG, "permission missing for PACKAGE_USAGE_STATS")
        }

        // Battery
        batteryMonitoring = BatteryMonitoring(this, firestore)

        // Network
        if (hasPhoneStatePermission) {
            networkMonitor =
                NetworkMonitor(this, connectivityManager, subscriptionManager, firestore)
        } else {
            Log.e(TAG, "Skipping network monitoring, Permission missing")
        }

        // Traffic
        trafficMonitor = TrafficMonitor(this, firestore).apply {
            start()
        }

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun stopService(name: Intent?): Boolean {
        isRunning = false

        // Stop services
        locationMonitoring?.stopServices()
        sensorsMonitoring?.stopServices()
        appStatsMonitoring?.stopServices()
        batteryMonitoring?.stopServices()
        networkMonitor?.stopServices()
        trafficMonitor?.stopServices()

        return super.stopService(name)
    }

    fun updateCallback(callback: ServiceCallback) {
        serviceCallback = callback
    }

    companion object {
        const val NOTIFICATION_ID = 99
        const val TAG = "ForegroundService"
    }
}