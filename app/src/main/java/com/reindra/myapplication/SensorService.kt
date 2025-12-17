package com.reindra.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var powerManager: PowerManager
    private lateinit var screenWakeLock: PowerManager.WakeLock
    private lateinit var cpuWakeLock: PowerManager.WakeLock
    private lateinit var notificationManager: NotificationManager

    // Shake detection variables
    private var lastUpdate: Long = 0
    private var lastShakeTime: Long = 0
    private var shakeCount = 0
    
    // Logging/Debug counters
    private var sensorEventCount = 0L
    private var lastLogTime = 0L

    companion object {
        const val TAG = "SensorService"
        const val CHANNEL_ID = "SensorServiceChannel"
        const val WAKE_LOCK_TAG = "DoubleTapToWeak:ScreenWakeLock"
        const val CPU_WAKE_LOCK_TAG = "DoubleTapToWeak:CpuWakeLock"
        private const val SHAKE_THRESHOLD = 800 // Sensitivity
        private const val DOUBLE_SHAKE_WINDOW = 1000 // ms
        private const val LOG_INTERVAL = 5000L // Log every 5 seconds
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "=== SERVICE onCreate STARTED ===")
        
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        notificationManager = getSystemService(NotificationManager::class.java)
        
        Log.i(TAG, "Accelerometer sensor: ${accelerometer?.name ?: "NULL - NOT FOUND!"}")
        Log.i(TAG, "Accelerometer vendor: ${accelerometer?.vendor ?: "N/A"}")
        
        // 1. CPU Lock: Keep CPU running to listen to sensors
        cpuWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CPU_WAKE_LOCK_TAG)
        cpuWakeLock.acquire()
        Log.i(TAG, "CPU WakeLock acquired: ${cpuWakeLock.isHeld}")

        // 2. Screen Lock: To turn on screen
        @Suppress("DEPRECATION")
        screenWakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKE_LOCK_TAG
        )
        Log.i(TAG, "Screen WakeLock created (not acquired yet)")

        createNotificationChannel()
        startForeground(1, createNotification(getString(R.string.notification_text_running)))
        Log.i(TAG, "Foreground service started with notification")
        
        accelerometer?.also { sensor ->
            val registered = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            Log.i(TAG, "Sensor listener registered: $registered")
            if (!registered) {
                Log.e(TAG, "FAILED TO REGISTER SENSOR LISTENER!")
            }
        } ?: run {
            Log.e(TAG, "NO ACCELEROMETER SENSOR AVAILABLE ON THIS DEVICE!")
        }
        
        Log.i(TAG, "=== SERVICE onCreate COMPLETED ===")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand called, flags=$flags, startId=$startId")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "=== SERVICE onDestroy STARTED ===")
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.i(TAG, "Sensor listener unregistered")
        
        if (cpuWakeLock.isHeld) {
            cpuWakeLock.release()
            Log.i(TAG, "CPU WakeLock released")
        }
        if (screenWakeLock.isHeld) {
            screenWakeLock.release()
            Log.i(TAG, "Screen WakeLock released")
        }
        Log.i(TAG, "=== SERVICE onDestroy COMPLETED ===")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                sensorEventCount++
                
                // Periodic logging to show sensor is still alive
                val now = System.currentTimeMillis()
                if (now - lastLogTime > LOG_INTERVAL) {
                    lastLogTime = now
                    val isScreenOn = powerManager.isInteractive
                    Log.d(TAG, ">>> Sensor ALIVE! Events: $sensorEventCount, Screen ON: $isScreenOn, CPU Lock held: ${cpuWakeLock.isHeld}")
                    updateNotification(getString(R.string.notification_text_events, sensorEventCount, if (isScreenOn) "ON" else "OFF"))
                }
                
                detectShake(it)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    private fun detectShake(event: SensorEvent) {
        val curTime = System.currentTimeMillis()
        if ((curTime - lastUpdate) > 100) {
            val diffTime = (curTime - lastUpdate)
            lastUpdate = curTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                Log.d(TAG, "!!! SHAKE DETECTED! Speed: $speed, Threshold: $SHAKE_THRESHOLD")
                
                val timeSinceLastShake = curTime - lastShakeTime
                Log.d(TAG, "Time since last shake: ${timeSinceLastShake}ms (window: ${DOUBLE_SHAKE_WINDOW}ms)")
                
                if (timeSinceLastShake < DOUBLE_SHAKE_WINDOW) {
                    shakeCount++
                    Log.d(TAG, "Shake count incremented to: $shakeCount")
                    
                    if (shakeCount >= 1) { // 2 shakes
                        Log.i(TAG, "*** DOUBLE SHAKE DETECTED! Calling wakeUpScreen() ***")
                        wakeUpScreen()
                        shakeCount = 0
                    }
                } else {
                    Log.d(TAG, "Shake too slow, resetting count")
                    shakeCount = 0
                }
                lastShakeTime = curTime
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    private fun wakeUpScreen() {
        val isScreenOn = powerManager.isInteractive
        Log.i(TAG, "wakeUpScreen() called. Screen currently ON: $isScreenOn")
        
        // Vibrate for feedback
        Log.d(TAG, "Attempting to vibrate...")
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                 vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                 @Suppress("DEPRECATION")
                 vibrator.vibrate(200)
            }
            Log.d(TAG, "Vibration triggered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Vibration FAILED: ${e.message}")
        }

        // Wake lock attempt
        Log.d(TAG, "Attempting to acquire screen wake lock...")
        try {
            if (!screenWakeLock.isHeld) {
                screenWakeLock.acquire(3000) // 3 second pulse to wake
                Log.i(TAG, "Screen WakeLock ACQUIRED for 3 seconds")
            } else {
                Log.w(TAG, "Screen WakeLock was already held!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Screen WakeLock acquire FAILED: ${e.message}")
        }
        
        // Check result
        val isScreenOnAfter = powerManager.isInteractive
        Log.i(TAG, "wakeUpScreen() completed. Screen now ON: $isScreenOnAfter")
        
        if (!isScreenOnAfter) {
            Log.w(TAG, "!!! SCREEN DID NOT WAKE UP - This may be a Samsung/OEM limitation !!!")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Double Tap Sensor Service",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(status: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_menu_rotate) 
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(status: String) {
        notificationManager.notify(1, createNotification(status))
    }
}

