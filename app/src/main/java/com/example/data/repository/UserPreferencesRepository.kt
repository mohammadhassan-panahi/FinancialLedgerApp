package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val IS_ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("is_onboarding_completed")
        private val IS_PRIVACY_MODE_ENABLED_KEY = booleanPreferencesKey("is_privacy_mode_enabled")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_ONBOARDING_COMPLETED_KEY] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETED_KEY] = completed
        }
    }

    /** "حالت مخفی‌سازی مبالغ" — when on, monetary figures render masked (••••) until toggled off. */
    val isPrivacyModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_PRIVACY_MODE_ENABLED_KEY] ?: false }

    suspend fun setPrivacyModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PRIVACY_MODE_ENABLED_KEY] = enabled
        }
    }
}
