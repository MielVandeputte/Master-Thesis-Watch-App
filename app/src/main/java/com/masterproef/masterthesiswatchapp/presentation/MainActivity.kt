package com.masterproef.masterthesiswatchapp.presentation

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import com.masterproef.masterthesiswatchapp.model.ForegroundService
import com.masterproef.masterthesiswatchapp.presentation.theme.MasterThesisWatchAppTheme

// TO DO: prevent crashes if toggling quickly back and forth by checking when service is done starting
// Auto toggle when service crashes / is forcefully turned off

class MainActivity : ComponentActivity() {

    val switchState = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(activity = this)
        }
    }

    fun setConnectable() {

        if (checkPermissions()) {

            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

            // Does device support bluetooth? If it's not, toast
            if (bluetoothManager.adapter != null) {

                // Is bluetooth turned on? If it's not, turn on
                if (bluetoothManager.adapter.isEnabled) {
                    startService()

                } else {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    val resultLauncher = registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            startService()
                        } else {
                            Toast.makeText(
                                this, "Bluetooth needs to be turned on.", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    resultLauncher.launch(enableBtIntent)
                }
            } else {
                Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            askPermissions()
        }
    }

    fun setUnconnectable() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        stopService(serviceIntent)

        switchState.value = (false)
    }

    private fun checkPermissions(): Boolean {

        val permissionChecks = mutableListOf<Int>()

        permissionChecks.add(
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADMIN
            )
        )
        permissionChecks.add(ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionChecks.add(
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.FOREGROUND_SERVICE
                )
            )
            permissionChecks.add(
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        }

        for (el in permissionChecks) {
            if (el != PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    private fun askPermissions() {

        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.WAKE_LOCK,
            ), 0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.BLUETOOTH_ADVERTISE
                ), 0
            )
        }
    }

    private fun startService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startForegroundService(serviceIntent)
        switchState.value = true
    }
}

@Composable
fun WearApp(activity: MainActivity) {

    val checked by activity.switchState.observeAsState()

    MasterThesisWatchAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Master Thesis Watch App", softWrap = false, fontSize = 12.sp
            )

            Switch(
                checked = checked!!,
                onCheckedChange = {
                    if (checked!!) {
                        activity.setUnconnectable()
                    } else {
                        activity.setConnectable()
                    }
                },
            )
        }
    }
}