package com.example.maccproj

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private val USERNAME_KEY = stringPreferencesKey("userName")

    suspend fun saveUsername(context: Context, userName: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = userName
        }
    }

    fun getUsername(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    }

    suspend fun removeUsername(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(USERNAME_KEY)
        }
    }
}
