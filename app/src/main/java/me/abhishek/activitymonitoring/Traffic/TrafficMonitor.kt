package me.abhishek.activitymonitoring.traffic

import android.content.Context
import android.net.LocalServerSocket
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import me.abhishek.activitymonitoring.service.ForegroundService
import me.abhishek.activitymonitoring.utils.Constatnts
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class TrafficMonitor(
    private val context: Context,
    private val firestore: FirebaseFirestore
) : Thread() {

    private var serverSocket: LocalServerSocket? = LocalServerSocket(context.packageName)

    override fun run() {
        getTraffic()
    }

    private fun getTraffic() {
        try {
            // Accept client connections and perform socket operations here
            while (true) {
                val socket = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                val line = reader.readLine()
                val params = line.split(",").toTypedArray()
                val domainName = params[0]
                val packageName = getPackageNameFromUid(context, params[1].toInt())
                addTrafficToFirestore (packageName, domainName)
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException $e")
        } finally {
            serverSocket.close()
        }
    }

    fun getPackageNameFromUid(context: Context, uid: Int): String {
        val pm = context.packageManager
        val packageNames = pm.getPackagesForUid(uid)
        return if (!packageNames.isNullOrEmpty()) {
            packageNames[0]
        } else {
            // let us assume some system app called it
            "system"
        }
    }

    private fun addTrafficToFirestore(
        packageName: String,
        domain: String
    ) {
        val trafficData =
            hashMapOf(System.currentTimeMillis().toString() to domain)
        firestore.collection(ForegroundService.UNIQUE_ID).document(Constatnts.MONITORING_DOCUMENT)
            .collection(Constatnts.TRAFFIC_COLLECTION)
            .document(packageName)
            .set(trafficData, SetOptions.merge())
            .addOnFailureListener {
                Log.e(TAG, "Traffic Data upload failed $it")
            }
    }

    companion object {
        const val TAG = "TrafficMonitoring"
    }
}
