package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Extension property that creates a single DataStore instance per app. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Holds the user's persisted settings.
 */
data class UserPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val language: String = "English"
)

/**
 * Repository for user preferences backed by Jetpack DataStore.
 * Persists onboarding completion, notification preference, and language.
 */
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }

    /** Observable stream of current preferences. */
    val preferences: Flow<UserPreferences> = context.dataStore.data
        .map { prefs ->
            UserPreferences(
                hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                language = prefs[Keys.LANGUAGE] ?: "English"
            )
        }

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = true }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = false }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
