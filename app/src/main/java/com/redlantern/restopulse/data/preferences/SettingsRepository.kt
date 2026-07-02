package com.redlantern.restopulse.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.redlantern.restopulse.domain.AppSettings
import com.redlantern.restopulse.domain.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore("restopulse_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val maxGroupSize = intPreferencesKey("max_group_size")
        val exportStatus = stringPreferencesKey("export_status")
    }

    val settings: Flow<AppSettings> = context.settingsStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.valueOf(prefs[Keys.theme] ?: ThemeMode.SYSTEM.name),
            dynamicColor = prefs[Keys.dynamicColor] ?: true,
            maxGroupSize = prefs[Keys.maxGroupSize] ?: 250,
            lastExportStatus = prefs[Keys.exportStatus] ?: "Ready"
        )
    }

    suspend fun setTheme(mode: ThemeMode) = context.settingsStore.edit { it[Keys.theme] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.settingsStore.edit { it[Keys.dynamicColor] = enabled }
    suspend fun setMaxGroupSize(size: Int) = context.settingsStore.edit { it[Keys.maxGroupSize] = size.coerceIn(25, 1000) }
    suspend fun setExportStatus(status: String) = context.settingsStore.edit { it[Keys.exportStatus] = status }
}
