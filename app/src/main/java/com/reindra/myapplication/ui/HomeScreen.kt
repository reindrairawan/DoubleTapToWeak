package com.reindra.myapplication.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reindra.myapplication.LockAccessibilityService
import com.reindra.myapplication.SensorService
import com.reindra.myapplication.ui.components.PremiumCard

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign

@SuppressLint("BatteryLife")
@Composable
fun HomeScreen(
    wakeEnabled: Boolean,
    lockEnabled: Boolean,
    sensitivity: Float,
    tapToLockEnabled: Boolean,
    onWakeChanged: (Boolean) -> Unit,
    onLockChanged: (Boolean) -> Unit,
    onSensitivityChanged: (Float) -> Unit,
    onTapToLockChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    var isBatteryOptimizationIgnored by remember { mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Header
        Text(
            text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_title),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_subtitle),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // --- Battery Optimization Card (IMPORTANT for Samsung) ---
        if (!isBatteryOptimizationIgnored) {
            PremiumCard(
                title = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_battery_warning_title),
                subtitle = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_battery_warning_subtitle),
                gradientColors = listOf(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_battery_warning_desc),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                        // Recheck after user returns
                        isBatteryOptimizationIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_battery_button_disable))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_battery_samsung_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tap to Lock (In App) Card - with DEDICATED TAP ZONE
        PremiumCard(
            title = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_tap_lock_title),
            subtitle = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_tap_lock_subtitle),
            gradientColors = if (tapToLockEnabled) 
                listOf(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f), MaterialTheme.colorScheme.surface)
            else 
                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
        ) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Rounded.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Switch(
                    checked = tapToLockEnabled,
                    onCheckedChange = onTapToLockChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                )
            }
             if (tapToLockEnabled) {
                 Spacer(modifier = Modifier.height(12.dp))
                 // Dedicated Tap Zone - More reliable on Samsung
                 Box(
                     modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (isAccessibilityServiceEnabled(context, LockAccessibilityService::class.java)) {
                                        val intent = Intent(context, LockAccessibilityService::class.java).apply {
                                            action = LockAccessibilityService.ACTION_LOCK
                                        }
                                        context.startService(intent)
                                    }
                                }
                            )
                        },
                     contentAlignment = Alignment.Center
                 ) {
                     Text(
                         text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_tap_lock_hint),
                         style = MaterialTheme.typography.labelMedium,
                         textAlign = TextAlign.Center,
                         color = MaterialTheme.colorScheme.error
                     )
                 }
             }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Wake Card
        PremiumCard(
            title = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_wake_title),
            subtitle = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_wake_subtitle),
            gradientColors = if (wakeEnabled) 
                listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), MaterialTheme.colorScheme.surface)
            else 
                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Rounded.Vibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Switch(
                    checked = wakeEnabled,
                    onCheckedChange = { newState ->
                        onWakeChanged(newState)
                        val intent = Intent(context, SensorService::class.java)
                        if (newState) {
                            // Start Service
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        } else {
                            context.stopService(intent)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            if (wakeEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_wake_sensitivity), style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = sensitivity,
                    onValueChange = onSensitivityChanged,
                    valueRange = 10f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_wake_battery_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lock Card
        PremiumCard(
            title = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_lock_title),
            subtitle = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_lock_subtitle),
             gradientColors = if (lockEnabled)
                listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), MaterialTheme.colorScheme.surface)
            else
                listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
        ) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Icon(
                    imageVector = Icons.Rounded.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                 Button(
                     onClick = {
                         if (!isAccessibilityServiceEnabled(context, LockAccessibilityService::class.java)) {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                         } else {
                            onLockChanged(!lockEnabled) // Just toggle preference, service is always "on" if system enabled
                         }
                     },
                     colors = ButtonDefaults.buttonColors(
                         containerColor = if (isAccessibilityServiceEnabled(context, LockAccessibilityService::class.java)) 
                             MaterialTheme.colorScheme.secondaryContainer 
                         else 
                             MaterialTheme.colorScheme.primary
                     )
                 ) {
                     Text(
                         if (isAccessibilityServiceEnabled(context, LockAccessibilityService::class.java)) 
                             androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_lock_button_actions) 
                         else 
                             androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_lock_button_enable)
                     )
                 }
             }
             if (isAccessibilityServiceEnabled(context, LockAccessibilityService::class.java)) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                      text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_lock_active_msg),
                      style = MaterialTheme.typography.bodySmall
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Button(onClick = {
                      val intent = Intent(context, LockAccessibilityService::class.java).apply {
                          action = LockAccessibilityService.ACTION_LOCK
                      }
                      context.startService(intent)
                  }) {
                      Text(androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.home_lock_test_button)) 
                  }
             }
        }
        
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Developer Credit
        Text(
            text = androidx.compose.ui.res.stringResource(com.reindra.myapplication.R.string.developer_credit),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }


fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    for (enabledService in enabledServices) {
        val enabledServiceInfo = enabledService.resolveInfo.serviceInfo
        if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name)
            return true
    }
    return false
}
