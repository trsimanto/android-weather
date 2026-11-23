package dev.towhid.weather.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDto(
    val current: CurrentDto,
    val hourly: HourlyDto,
    val daily: DailyDto,
)

@Serializable
data class CurrentDto(
    val time: String,
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("relative_humidity_2m") val humidity: Int,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("wind_speed_10m") val windSpeed: Double,
    @SerialName("is_day") val isDay: Int = 1,
)

@Serializable
data class HourlyDto(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int?> = emptyList(),
)

@Serializable
data class DailyDto(
    val time: List<String>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val maxTemperature: List<Double>,
    @SerialName("temperature_2m_min") val minTemperature: List<Double>,
    @SerialName("precipitation_probability_max") val precipitationProbability: List<Int?> = emptyList(),
)

@Serializable
data class GeocodingDto(
    val results: List<GeocodingResultDto> = emptyList(),
)

@Serializable
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @SerialName("admin1") val region: String? = null,
)
