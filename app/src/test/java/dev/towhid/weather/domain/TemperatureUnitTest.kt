package dev.towhid.weather.domain

import dev.towhid.weather.domain.model.TemperatureUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class TemperatureUnitTest {

    @Test
    fun `celsius is passed through`() {
        assertEquals(21.4, TemperatureUnit.CELSIUS.convert(21.4), 0.0001)
    }

    @Test
    fun `converts celsius to fahrenheit`() {
        assertEquals(32.0, TemperatureUnit.FAHRENHEIT.convert(0.0), 0.0001)
        assertEquals(212.0, TemperatureUnit.FAHRENHEIT.convert(100.0), 0.0001)
        assertEquals(-40.0, TemperatureUnit.FAHRENHEIT.convert(-40.0), 0.0001)
    }

    @Test
    fun `formats rounded values with a degree sign`() {
        assertEquals("23°", TemperatureUnit.CELSIUS.format(22.6))
        assertEquals("73°", TemperatureUnit.FAHRENHEIT.format(22.6))
    }

    @Test
    fun `toggle switches between units`() {
        assertEquals(TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS.toggled())
        assertEquals(TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT.toggled())
    }
}
