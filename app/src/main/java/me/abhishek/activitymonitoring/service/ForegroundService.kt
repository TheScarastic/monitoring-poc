package me.abhishek.activitymonitoring.service

import android.app.Service
import android.content.Intent
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import me.abhishek.activitymonitoring.R
import me.abhishek.activitymonitoring.sensors.SensorsMonitoring
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
    lateinit var firestore: FirebaseFirestore

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create a notification
        val notification = NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setContentTitle(getString(R.string.foreground_service))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "service created")
        SensorsMonitoring(sensorManager, firestore)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val NOTIFICATION_ID = 99
        const val TAG = "ForegroundService"
    }
}