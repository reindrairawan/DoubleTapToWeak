package com.reindra.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that starts the SensorService on device boot
 * if the user had it enabled before the device was turned off.
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            Log.i(TAG, "=== BOOT COMPLETED RECEIVED ===")
            
            // Check if wake was enabled in preferences
            val prefs = PreferencesManager(context)
            
            // Use coroutine to read from DataStore
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val wakeEnabled = prefs.wakeEnabled.first()
                    Log.i(TAG, "WakeEnabled preference: $wakeEnabled")
                    
                    if (wakeEnabled) {
                        Log.i(TAG, "Starting SensorService on boot...")
                        val serviceIntent = Intent(context, SensorService::class.java)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        Log.i(TAG, "SensorService start command sent")
                    } else {
                        Log.d(TAG, "Wake was disabled, not starting service")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in BootReceiver: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
