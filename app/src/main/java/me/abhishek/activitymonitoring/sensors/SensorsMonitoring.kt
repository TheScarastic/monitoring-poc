package me.abhishek.activitymonitoring.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import me.abhishek.activitymonitoring.ActivityMonitoringApplication
import me.abhishek.activitymonitoring.utils.Constatnts

class SensorsMonitoring(
    private val sensorManager: SensorManager,
    private val firestore: FirebaseFirestore
) : SensorEventListener {

    private val oldSensorValuesMap = hashMapOf<String, MutableList<Float>>()

    init {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.register(this)
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.register(this)
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)?.register(this)
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.register(this)
        sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)?.register(this)
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.register(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                addDataToFirestore(Constatnts.ACCELEROMETER, event)
            }

            Sensor.TYPE_GYROSCOPE -> {
                addDataToFirestore(Constatnts.GYROSCOPE, event)
            }

            Sensor.TYPE_LIGHT -> {
                addDataToFirestore(Constatnts.LIGHT, event)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                addDataToFirestore(Constatnts.MAGNETIC_FIELD, event)
            }

            Sensor.TYPE_ORIENTATION -> {
                addDataToFirestore(Constatnts.ORIENTATION, event)
            }

            Sensor.TYPE_PROXIMITY -> {
                addDataToFirestore(Constatnts.PROXIMITY, event)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun addDataToFirestore(document: String, event: SensorEvent) {
        val sensorValues = event.values.toList()

        if (!oldSensorValuesMap.contains(document)) {
            // Initialise the map
            oldSensorValuesMap[document] = mutableListOf()
        }

        if (!oldSensorValuesMap.getValue(document).containsAll(sensorValues)) {
            // Update the current values into old map
            oldSensorValuesMap.getValue(document).addAll(sensorValues)

            val sensorData = hashMapOf(
                (event.timestamp.toString()) to sensorValues,
            )

            // Update to firestore
            firestore.collection(ActivityMonitoringApplication.UNIQUE_ID).document(Constatnts.MONITORING_DOCUMENT)
                .collection(Constatnts.SENSORS_COLLECTION)
                .document(document)
                .set(sensorData, SetOptions.merge())
                .addOnFailureListener {
                    Log.e(TAG, "Sensors Data upload failed $it")
                }
        }
    }

    /**
     * Extension to register a sensor with normal delay
     */
    private fun Sensor.register(sensorEventListener: SensorEventListener) {
        sensorManager.registerListener(
            sensorEventListener,
            this,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    companion object {
        const val TAG = "SensorsMonitoring"
    }
}