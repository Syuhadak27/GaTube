package com.nontontv.indonesia.data.local

import dagger.hilt.android.qualifiers.ApplicationContext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nontontv.indonesia.util.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Pengaturan aplikasi yang bisa diubah dari menu Pengaturan. */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.settingsDataStore

    private val kunciKualitas = stringPreferencesKey("kualitas_default")
    private val kunciKecepatan = floatPreferencesKey("kecepatan_default")
    private val kunciPip = booleanPreferencesKey("pip_otomatis")
    private val kunciBackground = booleanPreferencesKey("background_play")

    val kualitasDefault: Flow<String> = dataStore.data.map { it[kunciKualitas] ?: Config.KUALITAS_BAWAAN }
    val kecepatanDefault: Flow<Float> = dataStore.data.map { it[kunciKecepatan] ?: 1.0f }
    val pipOtomatis: Flow<Boolean> = dataStore.data.map { it[kunciPip] ?: true }
    val backgroundPlay: Flow<Boolean> = dataStore.data.map { it[kunciBackground] ?: true }

    suspend fun simpanKualitas(kualitas: String) {
        dataStore.edit { it[kunciKualitas] = kualitas }
    }

    suspend fun simpanKecepatan(kecepatan: Float) {
        dataStore.edit { it[kunciKecepatan] = kecepatan }
    }

    suspend fun simpanPipOtomatis(aktif: Boolean) {
        dataStore.edit { it[kunciPip] = aktif }
    }

    suspend fun simpanBackgroundPlay(aktif: Boolean) {
        dataStore.edit { it[kunciBackground] = aktif }
    }
}
