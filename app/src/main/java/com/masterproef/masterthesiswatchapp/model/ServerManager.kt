package com.masterproef.masterthesiswatchapp.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Context
import no.nordicsemi.android.ble.BleServerManager
import no.nordicsemi.android.ble.observer.ServerObserver

class ServerManager(val context: Context) : BleServerManager(context), ServerObserver {
    override fun initializeServer(): MutableList<BluetoothGattService> {
        TODO("Not yet implemented")
    }

    override fun onServerReady() {
        TODO("Not yet implemented")
    }

    override fun onDeviceConnectedToServer(device: BluetoothDevice) {
        TODO("Not yet implemented")
    }

    override fun onDeviceDisconnectedFromServer(device: BluetoothDevice) {
        TODO("Not yet implemented")
    }
}