package me.abhishek.activitymonitoring

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import me.abhishek.activitymonitoring.databinding.ActivityMainBinding
import me.abhishek.activitymonitoring.service.ForegroundService
import me.abhishek.activitymonitoring.utils.Constatnts

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startService.setOnClickListener {
            showLocationUseDialog()
        }
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
        val intent = Intent(this, ForegroundService::class.java).apply {
            putExtra(Constatnts.LOCATION_EXTRA, startLocation)
        }
        startForegroundService(intent)

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
        }
    }

    companion object {
        const val TAG = "MainActivity"
        const val LOCATION_RESULT_CODE = 1000
        const val LOCATION_RESULT_CODE_BACKGROUND = 1001
    }
}