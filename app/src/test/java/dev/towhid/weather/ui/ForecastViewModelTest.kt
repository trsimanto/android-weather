package dev.towhid.weather.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.towhid.weather.MainDispatcherRule
import dev.towhid.weather.data.remote.toDomain
import dev.towhid.weather.domain.usecase.ObserveSavedCitiesUseCase
import dev.towhid.weather.domain.usecase.RefreshForecastUseCase
import dev.towhid.weather.fakes.FakeSettingsRepository
import dev.towhid.weather.fakes.FakeWeatherRepository
import dev.towhid.weather.fakes.TestData
import dev.towhid.weather.ui.forecast.ForecastIntent
import dev.towhid.weather.ui.forecast.ForecastViewModel
import app.cash.turbine.ReceiveTurbine
import dev.towhid.weather.ui.forecast.ForecastUiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForecastViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeWeatherRepository().apply {
        cities.value = listOf(TestData.dhaka)
    }

    private fun viewModel() = ForecastViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_CITY_ID to TestData.dhaka.id)),
        observeSavedCities = ObserveSavedCitiesUseCase(repository),
        refreshForecast = RefreshForecastUseCase(repository),
        settings = FakeSettingsRepository(),
    )

    private fun cacheForecast() {
        repository.forecasts.value = mapOf(TestData.dhaka.id to TestData.forecastDto().toDomain(TestData.dhaka.id, 1L))
    }

    @Test
    fun `shows the cached forecast for the selected city`() = runTest {
        cacheForecast()
        viewModel().state.test {
            val state = awaitLoaded()
            assertEquals("Dhaka", state.city?.name)
            assertEquals(30.0, state.forecast!!.current.temperatureC, 0.0)
            assertFalse(state.showOfflineBanner)
        }
    }

    @Test
    fun `offline with cache shows the banner, not the error`() = runTest {
        cacheForecast()
        repository.goOffline()
        viewModel().state.test {
            val state = awaitLoaded()
            assertTrue(state.showOfflineBanner)
            assertFalse(state.showError)
        }
    }

    @Test
    fun `offline without cache shows the error state`() = runTest {
        repository.goOffline()
        viewModel().state.test {
            val state = awaitLoaded()
            assertTrue(state.showError)
            assertFalse(state.showOfflineBanner)
        }
    }

    @Test
    fun `pull to refresh calls the repository again`() = runTest {
        val vm = viewModel()
        assertEquals(1, repository.refreshCalls)
        vm.onIntent(ForecastIntent.Refresh)
        assertEquals(2, repository.refreshCalls)
    }

    @Test
    fun `next 24 hours starts at the current hour`() = runTest {
        cacheForecast()
        viewModel().state.test {
            val hours = awaitLoaded().forecast!!.next24Hours
            assertEquals(24, hours.size)
            assertEquals(10, hours.first().time.hour)
        }
    }

    /** Skips the initial placeholder emitted by stateIn before the first real state. */
    private suspend fun ReceiveTurbine<ForecastUiState>.awaitLoaded(): ForecastUiState {
        var item = awaitItem()
        while (item.isLoading) item = awaitItem()
        return item
    }
}
