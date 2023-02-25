package com.masterproef.masterthesiswatchapp.model

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import no.nordicsemi.android.ble.BleServerManager
import java.util.*

class ServerManager(context: Context) : BleServerManager(context) {

    private val servicesList: MutableList<BluetoothGattService> = emptyList<BluetoothGattService>().toMutableList()

    init {
        // Person Identification Service
        servicesList.add(
            service(
                UUID.fromString("07b2ac95-f87c-4fb8-a500-5674097e643d"),
                characteristic(
                    UUID.fromString("07b2ac95-f87c-4fb8-a500-5674097e643d"),
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ,

                )
            )
        )
    }

    override fun initializeServer(): List<BluetoothGattService> {
        return servicesList
    }
}