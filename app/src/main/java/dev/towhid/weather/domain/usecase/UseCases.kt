package dev.towhid.weather.domain.usecase

import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.model.CityWithWeather
import dev.towhid.weather.domain.model.Forecast
import dev.towhid.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveSavedCitiesUseCase @Inject constructor(private val repository: WeatherRepository) {
    operator fun invoke(): Flow<List<CityWithWeather>> =
        combine(repository.observeCities(), repository.observeForecasts()) { cities, forecasts ->
            cities.map { CityWithWeather(it, forecasts[it.id]) }
        }
}

class ObserveForecastUseCase @Inject constructor(private val repository: WeatherRepository) {
    operator fun invoke(cityId: Long): Flow<Forecast?> = repository.observeForecast(cityId)
}

class RefreshForecastUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(cityId: Long): Result<Unit> = repository.refreshForecast(cityId)
    suspend fun all(): Result<Unit> = repository.refreshAll()
}

class SearchCitiesUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(query: String): List<City> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return emptyList()
        return repository.searchCities(trimmed)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}

class AddCityUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(city: City) {
        repository.addCity(city)
        repository.refreshForecast(city.id)
    }
}

class RemoveCityUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(cityId: Long) = repository.removeCity(cityId)
}
