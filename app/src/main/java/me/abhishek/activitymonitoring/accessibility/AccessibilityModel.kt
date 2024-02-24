package me.abhishek.activitymonitoring.accessibility

data class AccessibilityModel(
    val eventType: String,
    val className: String,
    val textShown: String,
    val contentDescription: String,
    var boundsLeft: Int?,
    val boundsTop: Int?,
    val boundsRight: Int?,
    val boundsBottom: Int,
    @field: JvmField
    val isFieldPassword: Boolean,
)