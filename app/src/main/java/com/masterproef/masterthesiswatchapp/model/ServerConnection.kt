package com.masterproef.masterthesiswatchapp.model

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager

class ServerConnection(context: Context) : BleManager(context) {

    private var gattCallback: GattCallback? = null
    override fun log(priority: Int, messageRes: Int, vararg params: Any?) {
        Log.i("MIELMIELMILE",priority.toString() + messageRes.toString())

        Log.i("ad;flkj;asdkfaj;sdf", params.toString())
    }

    override fun getGattCallback(): BleManagerGattCallback {
        gattCallback = GattCallback()
        return gattCallback!!
    }

    fun sendCharacteristicChangedNotification(characteristic: BluetoothGattCharacteristic, bytes: ByteArray) {
        sendNotification(characteristic, bytes).enqueue()
    }

    private inner class GattCallback : BleManagerGattCallback() {

        // There are no services that we need from the connecting device, but
        // if there were, we could specify them here.
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            return true
        }

        override fun onServicesInvalidated() {
            return
        }


    }

}