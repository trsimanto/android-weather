package dev.towhid.weather

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.model.CityWithWeather
import dev.towhid.weather.ui.cities.CitiesContent
import dev.towhid.weather.ui.cities.CitiesIntent
import dev.towhid.weather.ui.cities.CitiesUiState
import dev.towhid.weather.ui.theme.WeatherTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CitiesScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun emptyStateIsShownWithoutCities() {
        composeRule.setContent {
            WeatherTheme { CitiesContent(CitiesUiState(isLoading = false), {}, {}, {}) }
        }
        composeRule.onNodeWithText("No cities yet").assertIsDisplayed()
    }

    @Test
    fun unitToggleSendsIntent() {
        val intents = mutableListOf<CitiesIntent>()
        val city = City(1, "Dhaka", "Bangladesh", 23.7, 90.4)
        composeRule.setContent {
            WeatherTheme {
                CitiesContent(
                    CitiesUiState(isLoading = false, cities = listOf(CityWithWeather(city, null))),
                    { intents += it }, {}, {},
                )
            }
        }
        composeRule.onNodeWithText("Dhaka").assertIsDisplayed()
        composeRule.onNodeWithText("°C").performClick()
        assertEquals(listOf<CitiesIntent>(CitiesIntent.ToggleUnit), intents)
    }
}
