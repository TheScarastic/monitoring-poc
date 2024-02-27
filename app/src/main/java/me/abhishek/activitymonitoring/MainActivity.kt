package me.abhishek.activitymonitoring

import android.app.AppOpsManager
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import me.abhishek.activitymonitoring.databinding.ActivityMainBinding
import me.abhishek.activitymonitoring.service.ForegroundService

class MainActivity : AppCompatActivity(), ServiceCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var serviceIntent: Intent

    private lateinit var foregroundService: ForegroundService

    private var permissionFlags = 0

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

        binding.details.text =
            getString(
                R.string.device_details,
                ActivityMonitoringApplication.UNIQUE_ID, Build.MODEL
            )
    }

    private fun updateServiceButton() {
        if (!foregroundService.isRunning) {
            binding.startService.apply {
                text = getString(R.string.start_service)
                setOnClickListener {
                    requestPhonePermission()
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.POST_NOTIFICATIONS
            ), NOTIFICATION_RESULT_STATE
        )
    }

    private fun requestPhonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_PHONE_STATE
            ), PHONE_RESULT_STATE
        )
    }

    private fun showLocationUseDialog() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_location_summary))
                .setTitle(getString(R.string.dialog_location_title))
                .setPositiveButton(getString(R.string.grant_location_permission)) { _, _ ->
                    grantLocationPermission()
                }
                .setNegativeButton(getString(R.string.deny_location_permisiion)) { _, _ ->
                    startService()
                }.apply {
                    create().show()
                }
        } else {
            setPermissionFlag(LOCATION_RESULT_CODE_BACKGROUND)
            startService()
        }
    }

    private fun showAppStatsDialog() {
        if (!checkUsageStatsPermission(this, applicationInfo)) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.dialog_stats_summary))
                .setTitle(getString(R.string.dialog_stats_title))
                .setPositiveButton(getString(R.string.grant_stats_permission)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    showLocationUseDialog()
                }
                .setNegativeButton(getString(R.string.deny_stats_permisiion)) { _, _ ->
                    showLocationUseDialog()
                }.apply {
                    create().show()
                }
        } else {
            showLocationUseDialog()
        }
    }

    private fun grantLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            ), LOCATION_RESULT_CODE
        )
    }

    private fun startService() {
        Log.i(TAG, "Starting service")
        serviceIntent.apply {
            putExtra(PERMISSION_EXTRA, permissionFlags)
        }
        startForegroundService(serviceIntent)
        Toast.makeText(
            this, getString(R.string.start_service_toast),
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
                    startService()
                }
            }
        } else if (requestCode == LOCATION_RESULT_CODE_BACKGROUND) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    setPermissionFlag(LOCATION_RESULT_CODE_BACKGROUND)
                    startService()
                }

                else -> {
                    startService()
                }
            }

        } else if (requestCode == PHONE_RESULT_STATE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission()
            } else {
                showAppStatsDialog()
            }

            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    setPermissionFlag(PHONE_RESULT_STATE)
                }
            }

        } else if (requestCode == NOTIFICATION_RESULT_STATE) {
            showAppStatsDialog()
        }
    }

    private fun setPermissionFlag(value: Int) {
        permissionFlags = permissionFlags or value
    }

    override fun serviceLifecycleUpdated() {
        updateServiceButton()
    }

    companion object {
        const val TAG = "MainActivity"

        const val LOCATION_RESULT_CODE = 2
        const val LOCATION_RESULT_CODE_BACKGROUND = 4
        const val PHONE_RESULT_STATE = 1
        const val NOTIFICATION_RESULT_STATE = 8

        const val PERMISSION_EXTRA = "permission_extra"

        fun checkUsageStatsPermission(context: Context, applicationInfo: ApplicationInfo): Boolean {
            val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )

            if (mode == AppOpsManager.MODE_DEFAULT) {
                val permissionCheck = context.checkCallingOrSelfPermission(
                    android.Manifest.permission.PACKAGE_USAGE_STATS
                )
                return permissionCheck == PackageManager.PERMISSION_GRANTED
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }
    }
}