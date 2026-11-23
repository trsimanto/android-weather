package dev.towhid.weather.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.towhid.weather.domain.model.TemperatureUnit
import dev.towhid.weather.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val temperatureUnit: Flow<TemperatureUnit> = dataStore.data.map { prefs ->
        prefs[UNIT_KEY]?.let { runCatching { TemperatureUnit.valueOf(it) }.getOrNull() } ?: TemperatureUnit.CELSIUS
    }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { it[UNIT_KEY] = unit.name }
    }

    private companion object {
        val UNIT_KEY = stringPreferencesKey("temperature_unit")
    }
}
