package com.masterproef.masterthesiswatchapp.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.masterproef.masterthesiswatchapp.R

class ForegroundService : Service() {

    private val TAG = "ForegroundService"

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothObserver: BroadcastReceiver

    private var serverManager: ServerManager? = null
    private var bleAdvertiseCallback: BleAdvertiser.Callback? = null

    // Called by startService
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "Foreground Service Started")

        // Setup as a foreground service
        // Persistent notification is needed to keep this service running
        val notificationChannel = NotificationChannel(
            ForegroundService::class.java.simpleName,
            "Foreground service",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationService = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationService.createNotificationChannel(notificationChannel)

        val notification =
            NotificationCompat.Builder(this, ForegroundService::class.java.simpleName)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("Master Thesis Service")
                .setContentText("Bluetooth Low Energy and Sensors are active")
                .setAutoCancel(true)

        startForeground(1, notification.build())

        // Observe OS state changes in BLE
        // Stop service if bluetooth is turned off
        bluetoothObserver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val bluetoothState = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE, -1
                        )
                        when (bluetoothState) {
                            BluetoothAdapter.STATE_OFF -> {
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        }
                    }
                }
            }
        }

        registerReceiver(bluetoothObserver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        // Start advertising based on if bluetooth is on
        bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager

        if (bluetoothManager.adapter?.isEnabled == true) {
            startAdvertising()
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // Called by stopService
    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(bluetoothObserver)
        stopAdvertising()
    }

    private fun startAdvertising() {
        serverManager = ServerManager(this)
        serverManager!!.open()

        bleAdvertiseCallback = BleAdvertiser.Callback()

        try {
            bluetoothManager.adapter?.bluetoothLeAdvertiser?.startAdvertising(
                BleAdvertiser.settings(), BleAdvertiser.advertiseData(), bleAdvertiseCallback!!
            )
        } catch (e: SecurityException) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopAdvertising() {
        try {
            bleAdvertiseCallback?.let {
                val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothManager.adapter.bluetoothLeAdvertiser?.stopAdvertising(it)
                bleAdvertiseCallback = null
            }

            serverManager?.close()
            serverManager = null
        } catch (e: SecurityException) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}