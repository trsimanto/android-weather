package dev.towhid.weather.fakes

import dev.towhid.weather.data.local.CityEntity
import dev.towhid.weather.data.local.ForecastEntity
import dev.towhid.weather.data.local.WeatherDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory stand-in for Room, including the cascade delete from cities to forecasts. */
class FakeWeatherDao : WeatherDao {
    private val cities = MutableStateFlow<List<CityEntity>>(emptyList())
    private val forecasts = MutableStateFlow<Map<Long, ForecastEntity>>(emptyMap())

    override fun observeCities(): Flow<List<CityEntity>> = cities.map { list -> list.sortedBy { it.addedAtMillis } }
    override suspend fun getCities(): List<CityEntity> = cities.value
    override suspend fun getCity(cityId: Long): CityEntity? = cities.value.firstOrNull { it.id == cityId }

    override suspend fun insertCity(city: CityEntity) {
        cities.update { list -> if (list.any { it.id == city.id }) list else list + city }
    }

    override suspend fun deleteCity(cityId: Long) {
        cities.update { list -> list.filterNot { it.id == cityId } }
        forecasts.update { it - cityId }
    }

    override fun observeForecasts(): Flow<List<ForecastEntity>> = forecasts.map { it.values.toList() }
    override fun observeForecast(cityId: Long): Flow<ForecastEntity?> = forecasts.map { it[cityId] }

    override suspend fun upsertForecast(forecast: ForecastEntity) {
        forecasts.update { it + (forecast.cityId to forecast) }
    }
}
