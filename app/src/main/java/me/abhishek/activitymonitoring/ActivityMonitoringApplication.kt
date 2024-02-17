package me.abhishek.activitymonitoring

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.abhishek.activitymonitoring.utils.NotificationUtils

@HiltAndroidApp
class ActivityMonitoringApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        // Create a new notification channel
        NotificationUtils.createNotificationChannel(applicationContext)
    }
}