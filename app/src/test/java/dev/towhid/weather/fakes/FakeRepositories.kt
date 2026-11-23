package dev.towhid.weather.fakes

import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.model.Forecast
import dev.towhid.weather.domain.model.TemperatureUnit
import dev.towhid.weather.domain.repository.SettingsRepository
import dev.towhid.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException

class FakeWeatherRepository : WeatherRepository {
    val cities = MutableStateFlow<List<City>>(emptyList())
    val forecasts = MutableStateFlow<Map<Long, Forecast>>(emptyMap())
    var refreshResult: Result<Unit> = Result.success(Unit)
    var searchResults: List<City> = emptyList()
    var refreshCalls = 0

    override fun observeCities(): Flow<List<City>> = cities
    override fun observeForecasts(): Flow<Map<Long, Forecast>> = forecasts
    override fun observeForecast(cityId: Long): Flow<Forecast?> = forecasts.map { it[cityId] }
    override suspend fun searchCities(query: String): List<City> = searchResults
    override suspend fun addCity(city: City) = cities.update { it + city }
    override suspend fun removeCity(cityId: Long) = cities.update { list -> list.filterNot { it.id == cityId } }

    override suspend fun refreshForecast(cityId: Long): Result<Unit> {
        refreshCalls++
        return refreshResult
    }

    override suspend fun refreshAll(): Result<Unit> {
        refreshCalls++
        return refreshResult
    }

    fun goOffline() {
        refreshResult = Result.failure(IOException("No network"))
    }
}

class FakeSettingsRepository(initial: TemperatureUnit = TemperatureUnit.CELSIUS) : SettingsRepository {
    private val unit = MutableStateFlow(initial)
    override val temperatureUnit: Flow<TemperatureUnit> = unit
    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        this.unit.value = unit
    }
}
