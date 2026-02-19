package com.github.clabersmith.sleepplayer.core.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.slotDataStore by preferencesDataStore(
    name = "slot_prefs"
)