package com.reindra.myapplication

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class LockAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to listen to events, just provide the action
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_LOCK) {
            performLock()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun performLock() {
        val result = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        Log.d(TAG, "Perform lock result: $result")
    }

    companion object {
        const val TAG = "LockAccessService"
        const val ACTION_LOCK = "com.reindra.myapplication.ACTION_LOCK"
    }
}
