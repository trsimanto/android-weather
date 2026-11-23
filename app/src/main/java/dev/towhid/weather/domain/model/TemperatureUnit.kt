package dev.towhid.weather.domain.model

import kotlin.math.roundToInt

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun convert(celsius: Double): Double = when (this) {
        CELSIUS -> celsius
        FAHRENHEIT -> celsius * 9.0 / 5.0 + 32.0
    }

    /** Rounded value with a degree sign, e.g. "23°". */
    fun format(celsius: Double): String = "${convert(celsius).roundToInt()}°"

    fun toggled(): TemperatureUnit = if (this == CELSIUS) FAHRENHEIT else CELSIUS
}
