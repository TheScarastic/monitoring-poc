package me.abhishek.activitymonitoring.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint
import me.abhishek.activitymonitoring.service.ForegroundService
import me.abhishek.activitymonitoring.utils.Constatnts
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rect = Rect().apply {
            rootInActiveWindow.getBoundsInScreen(this)
        }

        val accessibilityModel = AccessibilityModel(
            AccessibilityEvent.eventTypeToString(event.eventType),
            event.className.toString(),
            event.text.toString(),
            event.contentDescription.toString(),
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            event.isPassword,
        )
        addAccessibilityToFirestore(event, accessibilityModel)
    }

    override fun onInterrupt() {
    }

    private fun addAccessibilityToFirestore(
        event: AccessibilityEvent,
        accessibilityModel: AccessibilityModel
    ) {
        val accessibilityData = hashMapOf(System.currentTimeMillis().toString() to accessibilityModel)
        firestore.collection(ForegroundService.UNIQUE_ID).document(Constatnts.MONITORING_DOCUMENT)
            .collection(Constatnts.ACCESSIBILITY_COLLECTION)
            .document(event.packageName.toString())
            .set(accessibilityData, SetOptions.merge())
            .addOnFailureListener {
                Log.e(TAG, "Accessibility Data upload failed $it")
            }
    }

    companion object {
        const val TAG = "MonitoringAccessibilityService"
    }
}