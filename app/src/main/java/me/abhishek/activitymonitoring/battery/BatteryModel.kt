package me.abhishek.activitymonitoring.battery

data class BatteryModel(
    val batteryPercentage: String,
    @field:JvmField
    val isCharging : Boolean,
    val chargingSource: String,
    val batteryHealth: String,
)