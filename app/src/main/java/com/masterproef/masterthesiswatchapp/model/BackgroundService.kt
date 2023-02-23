package com.masterproef.masterthesiswatchapp.model

import android.annotation.SuppressLint
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
import androidx.core.app.NotificationCompat
import com.masterproef.masterthesiswatchapp.R
import java.util.*

@SuppressLint("MissingPermission")
class BackgroundService : Service(){

    private val bluetoothManager: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private lateinit var bluetoothObserver: BroadcastReceiver

    private var serverManager: ServerManager? = null
    private var bleAdvertiseCallback: BleAdvertiser.Callback? = null

    // Called by startService
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Setup as a foreground service
        // Persistent notification is needed to keep this service running
        val notificationChannel = NotificationChannel(
            BackgroundService::class.java.simpleName,
            "Background service",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationService.createNotificationChannel(notificationChannel)

        val notification = NotificationCompat.Builder(this, BackgroundService::class.java.simpleName)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Master Thesis Service")
            .setContentText("Bluetooth Low Energy and Sensors are active")
            .setAutoCancel(true)

        startForeground(1, notification.build())

        // Observe OS state changes in BLE
        // Start or stop advertising if bluetooth is enabled or disabled
        bluetoothObserver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val bluetoothState = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE,
                            -1
                        )
                        when (bluetoothState) {
                            BluetoothAdapter.STATE_ON -> startAdvertising()
                            BluetoothAdapter.STATE_OFF -> stopAdvertising()
                        }
                    }
                }
            }
        }

        registerReceiver(bluetoothObserver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        // Start advertising based on if bluetooth is on
        if(bluetoothManager.adapter?.isEnabled == true) startAdvertising()

        return super.onStartCommand(intent, flags, startId)
    }

    // Called by stopService
    override fun onDestroy() {
        super.onDestroy()

        stopAdvertising()
        unregisterReceiver(bluetoothObserver)
    }

    private fun startAdvertising() {
        serverManager = ServerManager(this)
        serverManager!!.open()

        bleAdvertiseCallback = BleAdvertiser.Callback()

        bluetoothManager.adapter.bluetoothLeAdvertiser?.startAdvertising(
            BleAdvertiser.settings(),
            BleAdvertiser.advertiseData(),
            bleAdvertiseCallback!!
        )
    }

    private fun stopAdvertising() {
        bleAdvertiseCallback?.let {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter.bluetoothLeAdvertiser?.stopAdvertising(it)
            bleAdvertiseCallback = null
        }

        serverManager?.close()
        serverManager = null
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    object MyServiceProfile {
        val MY_SERVICE_UUID: UUID = UUID.fromString("be70e377-8a49-4f51-b51a-6be3f79f92fc")
    }

}