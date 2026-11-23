package dev.towhid.weather.domain

import dev.towhid.weather.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `maps WMO codes to conditions`() {
        val expected = mapOf(
            0 to WeatherCondition.CLEAR,
            1 to WeatherCondition.MAINLY_CLEAR,
            2 to WeatherCondition.PARTLY_CLOUDY,
            3 to WeatherCondition.OVERCAST,
            45 to WeatherCondition.FOG,
            48 to WeatherCondition.FOG,
            53 to WeatherCondition.DRIZZLE,
            57 to WeatherCondition.FREEZING_DRIZZLE,
            63 to WeatherCondition.RAIN,
            67 to WeatherCondition.FREEZING_RAIN,
            75 to WeatherCondition.SNOW,
            77 to WeatherCondition.SNOW_GRAINS,
            81 to WeatherCondition.RAIN_SHOWERS,
            86 to WeatherCondition.SNOW_SHOWERS,
            95 to WeatherCondition.THUNDERSTORM,
            99 to WeatherCondition.THUNDERSTORM_HAIL,
        )
        expected.forEach { (code, condition) ->
            assertEquals("code $code", condition, WeatherCondition.fromWmoCode(code))
        }
    }

    @Test
    fun `unknown codes fall back to UNKNOWN`() {
        listOf(-1, 4, 50, 100, 999).forEach {
            assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(it))
        }
    }

    @Test
    fun `clear sky has a different night emoji`() {
        assertNotEquals(WeatherCondition.CLEAR.emoji(isDay = true), WeatherCondition.CLEAR.emoji(isDay = false))
    }

    @Test
    fun `every condition has a label and emoji`() {
        WeatherCondition.entries.forEach {
            assert(it.label.isNotBlank()) { "$it label" }
            assert(it.emoji(true).isNotBlank()) { "$it emoji" }
        }
    }
}
