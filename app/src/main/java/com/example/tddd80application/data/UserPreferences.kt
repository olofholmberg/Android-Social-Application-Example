package com.example.tddd80application.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences (context: Context) {

    private val applicationContext = context.applicationContext
    private val dataStore: DataStore<Preferences>

    init {
        dataStore = applicationContext.createDataStore(
            name = "token_data_store"
        )
    }

    val accessToken: Flow<String?>
        get() = dataStore.data.map {preferences ->
            preferences[KEY_ACC_TOK]
        }

    suspend fun saveAccessToken(accessToken: String) {
        dataStore.edit {preferences ->
            preferences[KEY_ACC_TOK] = accessToken
        }
    }

    companion object {
        private val KEY_ACC_TOK = stringPreferencesKey("key_secret_access_token")
    }

}