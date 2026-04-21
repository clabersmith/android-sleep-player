package com.github.clabersmith.sleepplayer.core.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.sfxSlotDataStore by preferencesDataStore(
    name = "sfx_slot_prefs"
)