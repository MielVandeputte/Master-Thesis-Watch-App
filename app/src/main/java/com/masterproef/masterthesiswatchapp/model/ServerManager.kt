package com.masterproef.masterthesiswatchapp.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleServerManager
import no.nordicsemi.android.ble.observer.ServerObserver
import java.math.BigInteger

class ServerManager(private val context: Context) : BleServerManager(context), ServerObserver {

    private val serviceList = mutableMapOf<String, BluetoothGattService>()
    private val serverConnections = mutableMapOf<String, ServerConnection>()

    init {
        // Person Identification Service
        serviceList["id"] = service(
            Identifiers.identificationServiceId, characteristic(
                Identifiers.identificationCharacteristicId,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ,
            )
        )

        // Presence Service
        serviceList["presence"] = service(
            Identifiers.presenceServiceId
        )

        // Heart Analysis Service
        serviceList["heart"] = service(
            Identifiers.heartServiceId
        )
    }

    override fun initializeServer(): List<BluetoothGattService> {
        setServerObserver(this)

        val list = mutableListOf<BluetoothGattService>()
        serviceList.values.addAll(list)
        return list
    }

    override fun onDeviceConnectedToServer(device: BluetoothDevice) {

        serverConnections[device.address] = ServerConnection(context).apply {
            useServer(this@ServerManager)
            connect(device).enqueue()
        }

        val bytes: ByteArray = BigInteger.valueOf(0).toByteArray()
        val idCharacteristic = serviceList["id"]?.getCharacteristic(Identifiers.identificationCharacteristicId)

        if (idCharacteristic != null) {
            serverConnections[device.address]?.sendCharacteristicChangedNotification(idCharacteristic, bytes)
        }
    }

    override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
        serverConnections.remove(device.address)?.close()
    }

    override fun onServerReady() {
        return
    }
}