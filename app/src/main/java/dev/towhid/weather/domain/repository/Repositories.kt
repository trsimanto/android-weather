package dev.towhid.weather.domain.repository

import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.model.Forecast
import dev.towhid.weather.domain.model.TemperatureUnit
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun observeCities(): Flow<List<City>>
    fun observeForecasts(): Flow<Map<Long, Forecast>>
    fun observeForecast(cityId: Long): Flow<Forecast?>
    suspend fun searchCities(query: String): List<City>
    suspend fun addCity(city: City)
    suspend fun removeCity(cityId: Long)

    /** Fetches fresh data and writes it to the local database (the single source of truth). */
    suspend fun refreshForecast(cityId: Long): Result<Unit>
    suspend fun refreshAll(): Result<Unit>
}

interface SettingsRepository {
    val temperatureUnit: Flow<TemperatureUnit>
    suspend fun setTemperatureUnit(unit: TemperatureUnit)
}
