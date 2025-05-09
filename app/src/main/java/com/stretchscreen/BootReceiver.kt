package com.stretchscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build

/**
 * Broadcast receiver that starts the display service on device boot 
 * if it was enabled when the device was shut down
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPreferences: SharedPreferences = 
                context.getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE)
            
            val serviceEnabled = sharedPreferences.getBoolean("service_enabled", false)
            
            if (serviceEnabled) {
                // Start the DisplayService
                val serviceIntent = Intent(context, DisplayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
