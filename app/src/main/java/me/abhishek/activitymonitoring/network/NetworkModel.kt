package me.abhishek.activitymonitoring.network

import android.net.NetworkCapabilities

data class NetworkModel(
    val transport: List<String>?,
    val linkUpBandwidthKbps: String,
    val linkDownBandwidthKbps: String,
    @field: JvmField
    val isMetered: Boolean,
    val wifiName: String,
    val sim1Carrier: String,
    val sim1MCC: String,
    val sim1MNC: String,
    val sim2Carrier: String,
    val sim2MCC: String,
    val sim2MNC: String,
    )
