package me.abhishek.activitymonitoring

import android.app.Application
import android.os.Build
import android.provider.Settings
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.HiltAndroidApp
import me.abhishek.activitymonitoring.utils.Constatnts
import me.abhishek.activitymonitoring.utils.NotificationUtils
import javax.inject.Inject

@HiltAndroidApp
class ActivityMonitoringApplication : Application() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()
        // Create a new notification channel
        NotificationUtils.createNotificationChannel(applicationContext)

        UNIQUE_ID =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        addDeviceToFirestore()
    }

    private fun addDeviceToFirestore() {
        val deviceName = Build.MODEL
        firestore.collection(Constatnts.DEVICE_ID_DOCUMENT).document(Constatnts.DEVICE_ID_DOCUMENT)
            .set(hashMapOf(UNIQUE_ID to deviceName), SetOptions.merge() )
    }

    companion object {
        lateinit var UNIQUE_ID: String
    }
}