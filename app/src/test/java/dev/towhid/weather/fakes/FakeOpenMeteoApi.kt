package dev.towhid.weather.fakes

import dev.towhid.weather.data.remote.ForecastDto
import dev.towhid.weather.data.remote.GeocodingDto
import dev.towhid.weather.data.remote.OpenMeteoApi
import java.io.IOException

class FakeOpenMeteoApi : OpenMeteoApi {
    var forecast: ForecastDto = TestData.forecastDto()
    var geocoding: GeocodingDto = GeocodingDto()
    var failWith: Exception? = null
    var forecastCalls = 0

    override suspend fun forecast(
        latitude: Double,
        longitude: Double,
        current: String,
        hourly: String,
        daily: String,
        timezone: String,
        forecastDays: Int,
    ): ForecastDto {
        forecastCalls++
        failWith?.let { throw it }
        return forecast
    }

    override suspend fun searchCities(name: String, count: Int, language: String): GeocodingDto {
        failWith?.let { throw it }
        return geocoding
    }

    fun goOffline() {
        failWith = IOException("No network")
    }
}
