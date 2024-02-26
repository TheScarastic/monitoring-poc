package me.abhishek.activitymonitoring

import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import me.abhishek.activitymonitoring.databinding.ActivityMainBinding
import me.abhishek.activitymonitoring.service.ForegroundService
import me.abhishek.activitymonitoring.utils.Constatnts

class MainActivity : AppCompatActivity(), ServiceCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var serviceIntent: Intent

    private lateinit var foregroundService: ForegroundService

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as ForegroundService.LocalBinder
            foregroundService = binder.getService()
            foregroundService.updateCallback(this@MainActivity)
            updateServiceButton()
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            updateServiceButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serviceIntent = Intent(this, ForegroundService::class.java)

        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

    }

    private fun updateServiceButton() {
        if (!foregroundService.isRunning) {
            binding.startService.apply {
                text = getString(R.string.start_service)
                setOnClickListener {
                    requestCallPermission()
                }
            }
        } else {
            binding.startService.apply {
                text = getString(R.string.stop_service)
                setOnClickListener {
                    foregroundService.stopService(serviceIntent)
                    foregroundService.stopForeground(STOP_FOREGROUND_REMOVE)
                    runOnUiThread {
                        Toast.makeText(
                            context, getString(R.string.stop_service_toast),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun requestCallPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_PHONE_STATE
            ), PHONE_STATE
        )
    }


    private fun showLocationUseDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.dialog_location_summary))
            .setTitle(getString(R.string.dialog_location_title))
            .setPositiveButton(getString(R.string.grant_location_permission)) { _, _ ->
                grantLocationPermission()
            }
            .setNegativeButton(getString(R.string.deny_location_permisiion)) { _, _ ->
                startService(false)
            }.apply {
                create().show()
            }

    }

    private fun grantLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                ), LOCATION_RESULT_CODE
            )
        } else {
            startService(true)
        }
    }

    private fun startService(startLocation: Boolean) {
        Log.i(TAG, "Starting service, location: $startLocation")
        serviceIntent.apply {
            putExtra(Constatnts.LOCATION_EXTRA, startLocation)
        }
        startForegroundService(serviceIntent)
        Toast.makeText(
            this, getString(R.string.start_service_toast, startLocation),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_RESULT_CODE) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ), LOCATION_RESULT_CODE_BACKGROUND
                    )
                }

                else -> {
                    startService(false)
                }
            }
        } else if (requestCode == LOCATION_RESULT_CODE_BACKGROUND) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    startService(true)
                }

                else -> {
                    startService(false)
                }
            }
        } else if (requestCode == PHONE_STATE) {
            showLocationUseDialog()
        }
    }

    override fun serviceLifecycleUpdated() {
        updateServiceButton()
    }

    companion object {
        const val TAG = "MainActivity"
        const val LOCATION_RESULT_CODE = 1000
        const val LOCATION_RESULT_CODE_BACKGROUND = 1001
        const val PHONE_STATE = 1002
    }
}