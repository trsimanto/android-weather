package dev.towhid.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM cities ORDER BY addedAtMillis ASC")
    fun observeCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities")
    suspend fun getCities(): List<CityEntity>

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCity(cityId: Long): CityEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCity(city: CityEntity)

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteCity(cityId: Long)

    @Query("SELECT * FROM forecasts")
    fun observeForecasts(): Flow<List<ForecastEntity>>

    @Query("SELECT * FROM forecasts WHERE cityId = :cityId")
    fun observeForecast(cityId: Long): Flow<ForecastEntity?>

    @Upsert
    suspend fun upsertForecast(forecast: ForecastEntity)
}
