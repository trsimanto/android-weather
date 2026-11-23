package dev.towhid.weather.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** Open-Meteo is free and needs no API key. Weather data by Open-Meteo.com (CC BY 4.0). */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7,
    ): ForecastDto

    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
    ): GeocodingDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,is_day"
        const val HOURLY_FIELDS = "temperature_2m,weather_code,precipitation_probability"
        const val DAILY_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
    }
}
