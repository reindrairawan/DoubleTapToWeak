package com.reindra.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_WAKE_ENABLED = booleanPreferencesKey("wake_enabled")
        val KEY_LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
        val KEY_SENSITIVITY = floatPreferencesKey("sensitivity")
        val KEY_IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val KEY_TAP_TO_LOCK = booleanPreferencesKey("tap_to_lock")
    }

    val wakeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_WAKE_ENABLED] ?: false
        }

    val lockEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_LOCK_ENABLED] ?: false
        }

    val sensitivity: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_SENSITIVITY] ?: 50f
        }

    val isFirstRun: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_IS_FIRST_RUN] ?: true
        }

    val tapToLockEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_TAP_TO_LOCK] ?: false
        }

    suspend fun setWakeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WAKE_ENABLED] = enabled
        }
    }

    suspend fun setLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setSensitivity(value: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SENSITIVITY] = value
        }
    }

    suspend fun setFirstRunCompleted() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_FIRST_RUN] = false
        }
    }

    suspend fun setTapToLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TAP_TO_LOCK] = enabled
        }
    }
}
