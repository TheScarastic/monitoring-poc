package me.abhishek.activitymonitoring.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import me.abhishek.activitymonitoring.R

object NotificationUtils {

    fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                context.getString(R.string.channel_id),
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}