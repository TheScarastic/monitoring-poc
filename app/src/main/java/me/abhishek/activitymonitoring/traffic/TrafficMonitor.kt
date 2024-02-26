package me.abhishek.activitymonitoring.traffic

import android.content.Context
import android.net.LocalServerSocket
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import me.abhishek.activitymonitoring.ActivityMonitoringApplication
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
                if (serverSocket != null) {
                    val socket = serverSocket!!.accept()
                    val reader = BufferedReader(InputStreamReader(socket.inputStream))
                    val line = reader.readLine()
                    val params = line.split(",").toTypedArray()
                    val domainName = params[0]
                    val packageName = getPackageNameFromUid(context, params[1].toInt())
                    addTrafficToFirestore(packageName, domainName)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException $e")
        } finally {
            closeSocket()
        }
    }

    private fun getPackageNameFromUid(context: Context, uid: Int): String {
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
        firestore.collection(ActivityMonitoringApplication.UNIQUE_ID).document(Constatnts.MONITORING_DOCUMENT)
            .collection(Constatnts.TRAFFIC_COLLECTION)
            .document(packageName)
            .set(trafficData, SetOptions.merge())
            .addOnFailureListener {
                Log.e(TAG, "Traffic Data upload failed $it")
            }
    }

    private fun closeSocket() {
        // Known bug and workaround that LocalServerSocket::close is not working well
        // https://issuetracker.google.com/issues/36945762
        if (serverSocket != null) {
            try {
                Os.shutdown(serverSocket!!.fileDescriptor, OsConstants.SHUT_RDWR)
                serverSocket!!.close()
                serverSocket = null
            } catch (e: ErrnoException) {
                if (e.errno != OsConstants.EBADF) {
                    Log.w(TAG, "Socket already closed")
                } else {
                    Log.e(TAG, "Exception: cannot close DNS port on stop ${context.packageName} !")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: cannot close DNS port on stop ${context.packageName} !")
            }
        }
    }

    fun stopServices() {
        closeSocket()
        interrupt()
    }

    companion object {
        const val TAG = "TrafficMonitoring"
    }
}
