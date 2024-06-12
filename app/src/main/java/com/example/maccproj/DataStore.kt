package com.example.maccproj

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private val USER_ID_KEY = stringPreferencesKey("userid")
    private val USERNAME_KEY = stringPreferencesKey("username")

    suspend fun saveUserId(context: Context, userid: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userid
        }
    }

    fun getUserId(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun removeUserId(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    suspend fun saveUsername(context: Context, username: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = username
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
