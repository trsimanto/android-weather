package dev.towhid.weather.data

import dev.towhid.weather.data.local.ForecastEntity
import dev.towhid.weather.data.remote.GeocodingDto
import dev.towhid.weather.data.remote.GeocodingResultDto
import dev.towhid.weather.data.repository.WeatherRepositoryImpl
import dev.towhid.weather.domain.model.WeatherCondition
import dev.towhid.weather.fakes.FakeOpenMeteoApi
import dev.towhid.weather.fakes.FakeWeatherDao
import dev.towhid.weather.fakes.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    private lateinit var api: FakeOpenMeteoApi
    private lateinit var dao: FakeWeatherDao
    private lateinit var repository: WeatherRepositoryImpl
    private var now = 1_000L

    @Before
    fun setUp() {
        api = FakeOpenMeteoApi()
        dao = FakeWeatherDao()
        repository = WeatherRepositoryImpl(api, dao, Json { ignoreUnknownKeys = true }) { now }
    }

    @Test
    fun `refresh writes the network forecast into the cache`() = runTest {
        repository.addCity(TestData.dhaka)
        api.forecast = TestData.forecastDto(temperature = 31.5, code = 61)

        val result = repository.refreshForecast(TestData.dhaka.id)

        assertTrue(result.isSuccess)
        val cached = repository.observeForecast(TestData.dhaka.id).first()
        assertNotNull(cached)
        assertEquals(31.5, cached!!.current.temperatureC, 0.0)
        assertEquals(WeatherCondition.RAIN, cached.current.condition)
        assertEquals(now, cached.updatedAtMillis)
        assertEquals(7, cached.daily.size)
    }

    @Test
    fun `failed refresh keeps the cached forecast`() = runTest {
        repository.addCity(TestData.dhaka)
        repository.refreshForecast(TestData.dhaka.id)

        api.goOffline()
        now = 5_000L
        val result = repository.refreshForecast(TestData.dhaka.id)

        assertTrue(result.isFailure)
        val cached = repository.observeForecast(TestData.dhaka.id).first()
        assertNotNull(cached)
        assertEquals(1_000L, cached!!.updatedAtMillis)
    }

    @Test
    fun `refresh of an unknown city fails without calling the network`() = runTest {
        val result = repository.refreshForecast(42)

        assertTrue(result.isFailure)
        assertEquals(0, api.forecastCalls)
    }

    @Test
    fun `refreshAll refreshes every saved city`() = runTest {
        repository.addCity(TestData.dhaka)
        repository.addCity(TestData.london)

        repository.refreshAll()

        assertEquals(2, api.forecastCalls)
        assertEquals(setOf(TestData.dhaka.id, TestData.london.id), repository.observeForecasts().first().keys)
    }

    @Test
    fun `removing a city also removes its forecast`() = runTest {
        repository.addCity(TestData.dhaka)
        repository.refreshForecast(TestData.dhaka.id)

        repository.removeCity(TestData.dhaka.id)

        assertTrue(repository.observeCities().first().isEmpty())
        assertNull(repository.observeForecast(TestData.dhaka.id).first())
    }

    @Test
    fun `adding the same city twice keeps one entry`() = runTest {
        repository.addCity(TestData.dhaka)
        repository.addCity(TestData.dhaka)

        assertEquals(1, repository.observeCities().first().size)
    }

    @Test
    fun `search maps geocoding results to cities`() = runTest {
        api.geocoding = GeocodingDto(
            results = listOf(
                GeocodingResultDto(id = 1, name = "Dhaka", latitude = 23.7, longitude = 90.4, country = "Bangladesh", region = "Dhaka Division"),
            ),
        )

        val results = repository.searchCities("Dha")

        assertEquals(1, results.size)
        assertEquals("Dhaka Division, Bangladesh", results.single().region)
    }

    @Test
    fun `corrupt cache entry is ignored instead of crashing`() = runTest {
        repository.addCity(TestData.dhaka)
        dao.upsertForecast(ForecastEntity(TestData.dhaka.id, "{not json", 1L))

        assertNull(repository.observeForecast(TestData.dhaka.id).first())
        assertTrue(repository.observeForecasts().first().isEmpty())
    }
}
