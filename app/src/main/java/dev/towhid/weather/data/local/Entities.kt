package dev.towhid.weather.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.towhid.weather.domain.model.City

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val region: String,
    val latitude: Double,
    val longitude: Double,
    val addedAtMillis: Long,
)

/**
 * Latest forecast snapshot for a city. The API payload is stored as JSON so the
 * schema stays small; the UI only ever reads from this table (offline-first).
 */
@Entity(
    tableName = "forecasts",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["cityId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ForecastEntity(
    @PrimaryKey val cityId: Long,
    val payloadJson: String,
    val fetchedAtMillis: Long,
)

fun CityEntity.toDomain() = City(id, name, region, latitude, longitude)

fun City.toEntity(addedAtMillis: Long) = CityEntity(id, name, region, latitude, longitude, addedAtMillis)
