package dev.towhid.weather.data.repository

import dev.towhid.weather.data.local.ForecastEntity
import dev.towhid.weather.data.local.WeatherDao
import dev.towhid.weather.data.local.toDomain
import dev.towhid.weather.data.local.toEntity
import dev.towhid.weather.data.remote.ForecastDto
import dev.towhid.weather.data.remote.OpenMeteoApi
import dev.towhid.weather.data.remote.toDomain
import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.model.Forecast
import dev.towhid.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Offline-first repository: Room is the single source of truth.
 * Screens observe Room; network calls only ever write into Room.
 */
class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoApi,
    private val dao: WeatherDao,
    private val json: Json,
    private val clock: Clock,
) : WeatherRepository {

    override fun observeCities(): Flow<List<City>> =
        dao.observeCities().map { list -> list.map { it.toDomain() } }

    override fun observeForecasts(): Flow<Map<Long, Forecast>> =
        dao.observeForecasts().map { entities ->
            entities.mapNotNull { it.toDomainOrNull() }.associateBy { it.cityId }
        }

    override fun observeForecast(cityId: Long): Flow<Forecast?> =
        dao.observeForecast(cityId)
            .map { it?.toDomainOrNull() }
            .distinctUntilChanged()

    override suspend fun searchCities(query: String): List<City> =
        api.searchCities(query).results.map { it.toDomain() }

    override suspend fun addCity(city: City) {
        dao.insertCity(city.toEntity(addedAtMillis = clock.nowMillis()))
    }

    override suspend fun removeCity(cityId: Long) {
        dao.deleteCity(cityId)
    }

    override suspend fun refreshForecast(cityId: Long): Result<Unit> = runSuspendCatching {
        val city = dao.getCity(cityId) ?: error("City $cityId is not saved")
        val dto = api.forecast(city.latitude, city.longitude)
        dao.upsertForecast(
            ForecastEntity(
                cityId = cityId,
                payloadJson = json.encodeToString(ForecastDto.serializer(), dto),
                fetchedAtMillis = clock.nowMillis(),
            ),
        )
    }

    override suspend fun refreshAll(): Result<Unit> = coroutineScope {
        val results = dao.getCities()
            .map { city -> async { refreshForecast(city.id) } }
            .awaitAll()
        results.firstOrNull { it.isFailure } ?: Result.success(Unit)
    }

    private fun ForecastEntity.toDomainOrNull(): Forecast? = runCatching {
        json.decodeFromString(ForecastDto.serializer(), payloadJson).toDomain(cityId, fetchedAtMillis)
    }.getOrNull()
}

/** Like runCatching, but never swallows coroutine cancellation. */
internal inline fun <T> runSuspendCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

fun interface Clock {
    fun nowMillis(): Long
}
