package dev.towhid.weather.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.towhid.weather.data.repository.runSuspendCatching
import dev.towhid.weather.domain.model.City
import dev.towhid.weather.domain.usecase.AddCityUseCase
import dev.towhid.weather.domain.usecase.SearchCitiesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<City> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data class Select(val city: City) : SearchIntent
}

sealed interface SearchEvent {
    data object CityAdded : SearchEvent
}

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCities: SearchCitiesUseCase,
    private val addCity: AddCityUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _events = Channel<SearchEvent>(Channel.BUFFERED)
    val events: Flow<SearchEvent> = _events.receiveAsFlow()

    private val queries = MutableStateFlow("")

    init {
        queries
            .debounce(DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { _state.update { it.copy(isSearching = true, error = null) } }
            .mapLatest { runSuspendCatching { searchCities(it) } }
            .onEach { result ->
                _state.update {
                    it.copy(
                        isSearching = false,
                        results = result.getOrDefault(emptyList()),
                        error = result.exceptionOrNull()?.let { "Couldn't search — check your connection" },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                _state.update { it.copy(query = intent.query) }
                queries.value = intent.query
            }
            is SearchIntent.Select -> viewModelScope.launch {
                addCity(intent.city)
                _events.send(SearchEvent.CityAdded)
            }
        }
    }

    companion object {
        const val DEBOUNCE_MS = 350L
    }
}
