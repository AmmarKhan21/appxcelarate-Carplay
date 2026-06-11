package com.car.play.android.app.services

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.car.play.android.app.Activities.HomeActivity

class BluetoothReceiver : BroadcastReceiver() {

    companion object {
        private const val PREFS_NAME = "auto_launch_prefs"
        private const val KEY_ENABLED = "auto_launch_enabled"
        private const val KEY_DEVICE_MAC = "auto_launch_device_mac"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, false)) return

        val savedMac = prefs.getString(KEY_DEVICE_MAC, null) ?: return

        val device: BluetoothDevice? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        if (device?.address == savedMac) {
            val launchIntent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(launchIntent)
        }
    }
}
