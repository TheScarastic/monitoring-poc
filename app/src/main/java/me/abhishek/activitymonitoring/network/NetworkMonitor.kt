package me.abhishek.activitymonitoring.network

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.os.Build
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import me.abhishek.activitymonitoring.service.ForegroundService
import me.abhishek.activitymonitoring.utils.Constatnts

@RequiresApi(Build.VERSION_CODES.S)
class NetworkMonitor(
    private val context: Context,
    connectivityManager: ConnectivityManager,
    private val subscriptionManager: SubscriptionManager,
    private val firestore: FirebaseFirestore
) : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {

    init {
        val networkRequestBuilder = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .addTransportType(NetworkCapabilities.TRANSPORT_LOWPAN).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    addTransportType(NetworkCapabilities.TRANSPORT_USB)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    addTransportType(NetworkCapabilities.TRANSPORT_THREAD)
                }
            }.build()
        connectivityManager.registerNetworkCallback(networkRequestBuilder, this)

    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {

        val transport = transportToString(networkCapabilities)
        val subscription =
            if (context.checkCallingOrSelfPermission(android.Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                subscriptionManager.activeSubscriptionInfoList
            } else {
                null
            }

        val subscriptionInfo1 =
            if (subscription?.size != null && subscription.size > 0) {
                subscription[0]
            } else {
                null
            }

        val subscriptionInfo2 =
            if (subscription?.size != null && subscription.size > 1) {
                subscription[1]
            } else {
                null
            }

        val wifiSSID =
            if (networkCapabilities.transportInfo is WifiInfo) {
                (networkCapabilities.transportInfo as WifiInfo).ssid
            } else {
                null
            }

        val networkModel = NetworkModel(
            transport,
            "${networkCapabilities.linkDownstreamBandwidthKbps}kbps",
            "${networkCapabilities.linkUpstreamBandwidthKbps}kbps",
            !networkCapabilities.hasCapability(NET_CAPABILITY_NOT_METERED),
            wifiSSID ?: "",
            subscriptionInfo1?.displayName?.toString() ?: "",
            subscriptionInfo1?.mccString ?: "",
            subscriptionInfo1?.mncString ?: "",
            subscriptionInfo2?.displayName?.toString() ?: "",
            subscriptionInfo2?.mccString ?: "",
            subscriptionInfo2?.mncString ?: "",
        )

        addNetworkStatusToFirestore(networkModel)
    }

    private fun transportToString(networkCapabilities: NetworkCapabilities): List<String> {
        val transport = mutableListOf<String>()
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            transport.add("Cellular")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            transport.add("Wi-Fi")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
            transport.add("Bluetooth")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            transport.add("Ethernet")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            transport.add("VPN")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
            transport.add("Wi-Fi Aware")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)) {
            transport.add("Lowpan")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB)) {
            transport.add("USB")
        }
        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_THREAD)) {
            transport.add("Thread")
        }
        return transport
    }

    private fun addNetworkStatusToFirestore(networkModel: NetworkModel) {
        firestore.collection(ForegroundService.UNIQUE_ID).document(Constatnts.MONITORING_DOCUMENT)
            .collection(Constatnts.NETWORK_COLLECTION)
            .document(System.currentTimeMillis().toString())
            .set(networkModel)
            .addOnFailureListener {
                Log.e(TAG, "Network Data upload failed $it")
            }
    }

    companion object {
        const val TAG = "NetworkMonitor"
    }
}