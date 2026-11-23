package dev.towhid.weather.domain.model

/** WMO weather interpretation codes, as used by Open-Meteo. */
enum class WeatherCondition(val label: String, val dayEmoji: String, val nightEmoji: String = dayEmoji) {
    CLEAR("Clear sky", "☀️", "🌙"),
    MAINLY_CLEAR("Mainly clear", "🌤️", "🌙"),
    PARTLY_CLOUDY("Partly cloudy", "⛅", "☁️"),
    OVERCAST("Overcast", "☁️"),
    FOG("Fog", "🌫️"),
    DRIZZLE("Drizzle", "🌦️", "🌧️"),
    FREEZING_DRIZZLE("Freezing drizzle", "🌧️"),
    RAIN("Rain", "🌧️"),
    FREEZING_RAIN("Freezing rain", "🌧️"),
    SNOW("Snow", "🌨️"),
    SNOW_GRAINS("Snow grains", "🌨️"),
    RAIN_SHOWERS("Rain showers", "🌦️", "🌧️"),
    SNOW_SHOWERS("Snow showers", "🌨️"),
    THUNDERSTORM("Thunderstorm", "⛈️"),
    THUNDERSTORM_HAIL("Thunderstorm with hail", "⛈️"),
    UNKNOWN("Unknown", "❔");

    fun emoji(isDay: Boolean = true): String = if (isDay) dayEmoji else nightEmoji

    companion object {
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1 -> MAINLY_CLEAR
            2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            51, 53, 55 -> DRIZZLE
            56, 57 -> FREEZING_DRIZZLE
            61, 63, 65 -> RAIN
            66, 67 -> FREEZING_RAIN
            71, 73, 75 -> SNOW
            77 -> SNOW_GRAINS
            80, 81, 82 -> RAIN_SHOWERS
            85, 86 -> SNOW_SHOWERS
            95 -> THUNDERSTORM
            96, 99 -> THUNDERSTORM_HAIL
            else -> UNKNOWN
        }
    }
}
