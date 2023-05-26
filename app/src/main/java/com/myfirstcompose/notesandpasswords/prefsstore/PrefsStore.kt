package com.myfirstcompose.notesandpasswords.prefsstore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val FILTER_STATE = booleanPreferencesKey("filter_state")
val FILTER_VALUES = stringPreferencesKey("filter_values")
val NAP_LIST_TYPE = stringPreferencesKey("nap_list_type")




