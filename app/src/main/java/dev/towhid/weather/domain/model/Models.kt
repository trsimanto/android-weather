package dev.towhid.weather.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class City(
    val id: Long,
    val name: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
)

data class CurrentWeather(
    val time: LocalDateTime,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val windKmh: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperatureC: Double,
    val condition: WeatherCondition,
    val precipitationChance: Int,
)

data class DailyForecast(
    val date: LocalDate,
    val minC: Double,
    val maxC: Double,
    val condition: WeatherCondition,
    val precipitationChance: Int,
)

data class Forecast(
    val cityId: Long,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val updatedAtMillis: Long,
) {
    /** The next 24 hours starting from the current hour. */
    val next24Hours: List<HourlyForecast>
        get() = hourly
            .filter { !it.time.isBefore(current.time.withMinute(0)) }
            .take(24)
}

data class CityWithWeather(
    val city: City,
    val forecast: Forecast?,
)
