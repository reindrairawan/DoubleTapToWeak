package com.reindra.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.reindra.myapplication.ui.HomeScreen
import com.reindra.myapplication.ui.theme.DoubleTapToWeakTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = PreferencesManager(this)

        setContent {
            DoubleTapToWeakTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val wakeEnabled = prefs.wakeEnabled.collectAsState(initial = false)
                    val lockEnabled = prefs.lockEnabled.collectAsState(initial = false)
                    val sensitivity = prefs.sensitivity.collectAsState(initial = 50f)
                    val tapToLockEnabled = prefs.tapToLockEnabled.collectAsState(initial = false)
                    val isFirstRun = prefs.isFirstRun.collectAsState(initial = true)
                    val scope = rememberCoroutineScope()

                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        if (isFirstRun.value) {
                             com.reindra.myapplication.ui.OnboardingScreen(
                                 onFinish = {
                                     scope.launch { prefs.setFirstRunCompleted() }
                                 }
                             )
                        } else {
                            HomeScreen(
                                wakeEnabled = wakeEnabled.value,
                                lockEnabled = lockEnabled.value,
                                sensitivity = sensitivity.value,
                                tapToLockEnabled = tapToLockEnabled.value,
                                onWakeChanged = { enabled ->
                                    scope.launch { prefs.setWakeEnabled(enabled) }
                                },
                                onLockChanged = { enabled ->
                                    scope.launch { prefs.setLockEnabled(enabled) }
                                },
                                onSensitivityChanged = { value ->
                                    scope.launch { prefs.setSensitivity(value) }
                                },
                                onTapToLockChanged = { enabled ->
                                    scope.launch { prefs.setTapToLockEnabled(enabled) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}