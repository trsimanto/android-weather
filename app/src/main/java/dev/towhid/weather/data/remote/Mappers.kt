package dev.towhid.weather.data.remote

import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.model.CurrentWeather
import dev.towhid.weather.domain.model.DailyForecast
import dev.towhid.weather.domain.model.Forecast
import dev.towhid.weather.domain.model.HourlyForecast
import dev.towhid.weather.domain.model.WeatherCondition
import java.time.LocalDate
import java.time.LocalDateTime

fun GeocodingResultDto.toDomain(): City = City(
    id = id,
    name = name,
    region = listOfNotNull(region, country).distinct().joinToString(", "),
    latitude = latitude,
    longitude = longitude,
)

fun ForecastDto.toDomain(cityId: Long, updatedAtMillis: Long): Forecast = Forecast(
    cityId = cityId,
    current = CurrentWeather(
        time = LocalDateTime.parse(current.time),
        temperatureC = current.temperature,
        feelsLikeC = current.apparentTemperature,
        humidityPercent = current.humidity,
        windKmh = current.windSpeed,
        condition = WeatherCondition.fromWmoCode(current.weatherCode),
        isDay = current.isDay == 1,
    ),
    hourly = hourly.time.indices.map { i ->
        HourlyForecast(
            time = LocalDateTime.parse(hourly.time[i]),
            temperatureC = hourly.temperature[i],
            condition = WeatherCondition.fromWmoCode(hourly.weatherCode[i]),
            precipitationChance = hourly.precipitationProbability.getOrNull(i) ?: 0,
        )
    },
    daily = daily.time.indices.map { i ->
        DailyForecast(
            date = LocalDate.parse(daily.time[i]),
            minC = daily.minTemperature[i],
            maxC = daily.maxTemperature[i],
            condition = WeatherCondition.fromWmoCode(daily.weatherCode[i]),
            precipitationChance = daily.precipitationProbability.getOrNull(i) ?: 0,
        )
    },
    updatedAtMillis = updatedAtMillis,
)
