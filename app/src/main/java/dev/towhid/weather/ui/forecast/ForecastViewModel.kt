package dev.towhid.weather.ui.forecast

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.model.Forecast
import dev.towhid.weather.domain.model.TemperatureUnit
import dev.towhid.weather.domain.repository.SettingsRepository
import dev.towhid.weather.domain.usecase.ObserveSavedCitiesUseCase
import dev.towhid.weather.domain.usecase.RefreshForecastUseCase
import dev.towhid.weather.ui.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForecastUiState(
    val isLoading: Boolean = true,
    val city: City? = null,
    val forecast: Forecast? = null,
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val isRefreshing: Boolean = false,
    val refreshFailed: Boolean = false,
) {
    /** Cached data is on screen but the latest refresh could not reach the network. */
    val showOfflineBanner: Boolean get() = refreshFailed && forecast != null
    val showError: Boolean get() = refreshFailed && forecast == null && !isRefreshing
}

sealed interface ForecastIntent {
    data object Refresh : ForecastIntent
    data object ToggleUnit : ForecastIntent
}

@HiltViewModel
class ForecastViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeSavedCities: ObserveSavedCitiesUseCase,
    private val refreshForecast: RefreshForecastUseCase,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val cityId: Long = checkNotNull(savedStateHandle[Routes.ARG_CITY_ID])
    private val refreshing = MutableStateFlow(false)
    private val failed = MutableStateFlow(false)

    val state: StateFlow<ForecastUiState> = combine(
        observeSavedCities(),
        settings.temperatureUnit,
        refreshing,
        failed,
    ) { cities, unit, isRefreshing, refreshFailed ->
        val match = cities.firstOrNull { it.city.id == cityId }
        ForecastUiState(
            isLoading = false,
            city = match?.city,
            forecast = match?.forecast,
            unit = unit,
            isRefreshing = isRefreshing,
            refreshFailed = refreshFailed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ForecastUiState())

    init {
        onIntent(ForecastIntent.Refresh)
    }

    fun onIntent(intent: ForecastIntent) {
        when (intent) {
            ForecastIntent.Refresh -> {
                if (refreshing.value) return
                viewModelScope.launch {
                    refreshing.value = true
                    failed.value = refreshForecast(cityId).isFailure
                    refreshing.value = false
                }
            }
            ForecastIntent.ToggleUnit -> viewModelScope.launch {
                settings.setTemperatureUnit(settings.temperatureUnit.first().toggled())
            }
        }
    }
}
