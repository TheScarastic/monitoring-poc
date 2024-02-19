package me.abhishek.activitymonitoring.usagestats

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import me.abhishek.activitymonitoring.utils.Constatnts
import java.util.Calendar

class AppStatsMonitoring(
    private val usageStatsManager: UsageStatsManager,
    private val context: Context,
    private val firestore: FirebaseFirestore
) {

    private val handler: Handler = Handler(context.mainLooper)

    init {
        handler.post { getUsageStats() }
    }

    private fun getUsageStats() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)

        val stats =
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                cal.timeInMillis,
                System.currentTimeMillis()
            )

        val aggregatedStats = mutableMapOf<String, UsageStats>()
        val statCount = stats.size

        for (i in 0 until statCount) {
            val newStat = stats[i]
            val existingStat = aggregatedStats[newStat.packageName]

            if (existingStat == null) {
                aggregatedStats[newStat.packageName] = newStat
            } else {
                existingStat.add(newStat)
            }
        }

        if (aggregatedStats.isNotEmpty()) {
            val statsMap = aggregatedStats.entries

            statsMap
                .toList()
                .filter {
                    context.packageManager.getLaunchIntentForPackage(it.key) != null
                            && it.value.totalTimeInForeground > 0
                }
                .onEach { (_, stat) -> addAppsStatsToFirestore(stat) }
        } else {
            Log.i(TAG, "The aggregatedStats are empty can't do much")
        }

        handler.postDelayed({
            getUsageStats()
        }, DELAY_UPLOAD_INTERVAL_HR)
    }

    private fun addAppsStatsToFirestore(usageStats: UsageStats) {
        val appsData = hashMapOf(
            usageStats.packageName to usageStats.totalTimeInForeground,
        )
        firestore.collection(Constatnts.APPS_STATS_COLLECTION)
            .document(Constatnts.APPS_FOREGROUND_TIME)
            .set(appsData, SetOptions.merge())
            .addOnFailureListener {
                Log.e(TAG, "Apps stats Data upload failed $it")
            }
    }

    companion object {
        private const val TAG = "AppUsageStats"
        private const val DELAY_UPLOAD_INTERVAL_HR = 1 * 60 * 60 * 1000L // 1 hour
    }
}