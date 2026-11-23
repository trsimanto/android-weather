package dev.towhid.weather.fakes

import dev.towhid.weather.data.remote.CurrentDto
import dev.towhid.weather.data.remote.DailyDto
import dev.towhid.weather.data.remote.ForecastDto
import dev.towhid.weather.data.remote.HourlyDto
import dev.towhid.weather.domain.model.City

object TestData {
    val dhaka = City(id = 1185241, name = "Dhaka", region = "Dhaka Division, Bangladesh", latitude = 23.71, longitude = 90.41)
    val london = City(id = 2643743, name = "London", region = "England, United Kingdom", latitude = 51.51, longitude = -0.13)

    fun forecastDto(temperature: Double = 30.0, code: Int = 0) = ForecastDto(
        current = CurrentDto(
            time = "2026-06-01T10:15",
            temperature = temperature,
            apparentTemperature = temperature + 2,
            humidity = 70,
            weatherCode = code,
            windSpeed = 12.0,
            isDay = 1,
        ),
        hourly = HourlyDto(
            time = (0..47).map { "2026-06-%02dT%02d:00".format(1 + it / 24, it % 24) },
            temperature = (0..47).map { 25.0 + it % 10 },
            weatherCode = List(48) { code },
            precipitationProbability = List(48) { null },
        ),
        daily = DailyDto(
            time = (1..7).map { "2026-06-%02d".format(it) },
            weatherCode = List(7) { code },
            maxTemperature = List(7) { 33.0 },
            minTemperature = List(7) { 26.0 },
            precipitationProbability = List(7) { 10 },
        ),
    )
}
