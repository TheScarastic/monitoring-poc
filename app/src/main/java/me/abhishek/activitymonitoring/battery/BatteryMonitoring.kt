package me.abhishek.activitymonitoring.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import me.abhishek.activitymonitoring.service.ForegroundService.Companion.UNIQUE_ID
import me.abhishek.activitymonitoring.utils.Constatnts

class BatteryMonitoring(
    private val context: Context,
    private val firestore: FirebaseFirestore
) : BroadcastReceiver() {

    init {
        registerReceiver()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val source = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)


                addBatteryStatusToFirestore(
                    BatteryModel(
                        "${level * 100 / scale.toFloat()} %",
                        status == BatteryManager.BATTERY_STATUS_CHARGING
                                || status == BatteryManager.BATTERY_STATUS_FULL,
                        convertChargeSource(source),
                        convertBatteryHealth(health),
                    )
                )
            }
        }
    }

    private fun convertChargeSource(chargePlug: Int): String {
        return when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> {
                "AC"
            }

            BatteryManager.BATTERY_PLUGGED_DOCK -> {
                "DOCK"
            }

            BatteryManager.BATTERY_PLUGGED_USB -> {
                "USB"
            }

            BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                "WIRELESS"
            }

            else -> {
                "UNKNOWN"
            }
        }
    }

    private fun convertBatteryHealth(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_COLD -> {
                "COLD"
            }

            BatteryManager.BATTERY_HEALTH_DEAD -> {
                "DEAD"
            }

            BatteryManager.BATTERY_HEALTH_GOOD -> {
                "GOOD"
            }

            BatteryManager.BATTERY_HEALTH_OVERHEAT -> {
                "OVERHEAT"
            }

            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> {
                "OVER VOLTAGE"
            }

            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> {
                "UNSPECIFIED FAILURE"
            }

            else -> {
                "UNKNOWN"
            }
        }
    }

    private fun addBatteryStatusToFirestore(batteryModel: BatteryModel) {
        firestore.collection(UNIQUE_ID).document(Constatnts.MONITORING_DOCUMENT)
            .collection(Constatnts.BATTERY_COLLECTION)
            .document(System.currentTimeMillis().toString())
            .set(batteryModel)
            .addOnFailureListener {
                Log.e(TAG, "Battery Data upload failed $it")
            }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        context.registerReceiver(this, intentFilter)
    }

    companion object {
        const val TAG = "BatteryMonitoring"
    }
}