package dev.towhid.weather.ui

import app.cash.turbine.test
import dev.towhid.weather.MainDispatcherRule
import dev.towhid.weather.data.remote.toDomain
import dev.towhid.weather.domain.model.TemperatureUnit
import dev.towhid.weather.domain.usecase.ObserveSavedCitiesUseCase
import dev.towhid.weather.domain.usecase.RefreshForecastUseCase
import dev.towhid.weather.domain.usecase.RemoveCityUseCase
import dev.towhid.weather.fakes.FakeSettingsRepository
import dev.towhid.weather.fakes.FakeWeatherRepository
import dev.towhid.weather.fakes.TestData
import dev.towhid.weather.ui.cities.CitiesIntent
import dev.towhid.weather.ui.cities.CitiesViewModel
import app.cash.turbine.ReceiveTurbine
import dev.towhid.weather.ui.cities.CitiesUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CitiesViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeWeatherRepository()
    private val settings = FakeSettingsRepository()

    private fun viewModel() = CitiesViewModel(
        observeSavedCities = ObserveSavedCitiesUseCase(repository),
        refreshForecast = RefreshForecastUseCase(repository),
        removeCity = RemoveCityUseCase(repository),
        settings = settings,
    )

    @Test
    fun `refreshes all cities on start`() = runTest {
        viewModel()
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun `combines saved cities with their cached forecasts`() = runTest {
        repository.cities.value = listOf(TestData.dhaka, TestData.london)
        repository.forecasts.value = mapOf(TestData.dhaka.id to TestData.forecastDto().toDomain(TestData.dhaka.id, 1L))

        viewModel().state.test {
            val state = awaitLoaded()
            assertFalse(state.isLoading)
            assertEquals(2, state.cities.size)
            assertNotNull(state.cities.first { it.city.id == TestData.dhaka.id }.forecast)
            assertEquals(null, state.cities.first { it.city.id == TestData.london.id }.forecast)
        }
    }

    @Test
    fun `failed refresh with cached data marks the screen offline`() = runTest {
        repository.goOffline()
        repository.cities.value = listOf(TestData.dhaka)

        viewModel().state.test {
            assertTrue(awaitLoaded().isOffline)
        }
    }

    @Test
    fun `toggle unit persists the new unit`() = runTest {
        val vm = viewModel()
        vm.state.test {
            assertEquals(TemperatureUnit.CELSIUS, awaitLoaded().unit)
            vm.onIntent(CitiesIntent.ToggleUnit)
            assertEquals(TemperatureUnit.FAHRENHEIT, awaitLoaded().unit)
        }
    }

    @Test
    fun `remove deletes the city`() = runTest {
        repository.cities.value = listOf(TestData.dhaka, TestData.london)
        val vm = viewModel()
        vm.state.test {
            assertEquals(2, awaitLoaded().cities.size)
            vm.onIntent(CitiesIntent.Remove(TestData.dhaka.id))
            assertEquals(listOf(TestData.london), awaitLoaded().cities.map { it.city })
        }
    }

    /** Skips the initial placeholder emitted by stateIn before the first real state. */
    private suspend fun ReceiveTurbine<CitiesUiState>.awaitLoaded(): CitiesUiState {
        var item = awaitItem()
        while (item.isLoading) item = awaitItem()
        return item
    }
}
