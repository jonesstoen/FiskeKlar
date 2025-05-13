package no.uio.ifi.in2000.team46.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
// creates an extension property on context to access the preferences datastore named "settings"
// this can be used to store key-value pairs like user settings or preferences

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings") 