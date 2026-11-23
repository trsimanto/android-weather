package dev.towhid.weather.ui.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.towhid.weather.domain.model.CityWithWeather
import dev.towhid.weather.domain.model.TemperatureUnit
import dev.towhid.weather.domain.repository.SettingsRepository
import dev.towhid.weather.domain.usecase.ObserveSavedCitiesUseCase
import dev.towhid.weather.domain.usecase.RefreshForecastUseCase
import dev.towhid.weather.domain.usecase.RemoveCityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CitiesUiState(
    val isLoading: Boolean = true,
    val cities: List<CityWithWeather> = emptyList(),
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
)

sealed interface CitiesIntent {
    data object Refresh : CitiesIntent
    data object ToggleUnit : CitiesIntent
    data class Remove(val cityId: Long) : CitiesIntent
}

@HiltViewModel
class CitiesViewModel @Inject constructor(
    observeSavedCities: ObserveSavedCitiesUseCase,
    private val refreshForecast: RefreshForecastUseCase,
    private val removeCity: RemoveCityUseCase,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private val offline = MutableStateFlow(false)

    val state: StateFlow<CitiesUiState> = combine(
        observeSavedCities(),
        settings.temperatureUnit,
        refreshing,
        offline,
    ) { cities, unit, isRefreshing, isOffline ->
        CitiesUiState(
            isLoading = false,
            cities = cities,
            unit = unit,
            isRefreshing = isRefreshing,
            isOffline = isOffline,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CitiesUiState())

    init {
        onIntent(CitiesIntent.Refresh)
    }

    fun onIntent(intent: CitiesIntent) {
        when (intent) {
            CitiesIntent.Refresh -> refresh()
            CitiesIntent.ToggleUnit -> viewModelScope.launch {
                settings.setTemperatureUnit(settings.temperatureUnit.first().toggled())
            }
            is CitiesIntent.Remove -> viewModelScope.launch { removeCity(intent.cityId) }
        }
    }

    private fun refresh() {
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            offline.value = refreshForecast.all().isFailure
            refreshing.value = false
        }
    }
}
