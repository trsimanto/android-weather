package dev.towhid.weather.ui.cities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.towhid.weather.domain.model.CityWithWeather
import dev.towhid.weather.domain.model.TemperatureUnit
import dev.towhid.weather.ui.components.EmptyState
import dev.towhid.weather.ui.components.OfflineBanner

@Composable
fun CitiesScreen(
    onCityClick: (Long) -> Unit,
    onAddCity: () -> Unit,
    viewModel: CitiesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CitiesContent(state, viewModel::onIntent, onCityClick, onAddCity)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesContent(
    state: CitiesUiState,
    onIntent: (CitiesIntent) -> Unit,
    onCityClick: (Long) -> Unit,
    onAddCity: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Now") },
                actions = {
                    TextButton(onClick = { onIntent(CitiesIntent.ToggleUnit) }) {
                        Text(state.unit.symbol)
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCity,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add city") },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(CitiesIntent.Refresh) },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            if (!state.isLoading && state.cities.isEmpty()) {
                EmptyState("🌍", "No cities yet", "Tap \"Add city\" to follow the weather anywhere.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.isOffline && state.cities.any { it.forecast != null }) {
                        item { OfflineBanner() }
                    }
                    items(state.cities, key = { it.city.id }) { item ->
                        CityCard(
                            item = item,
                            unit = state.unit,
                            onClick = { onCityClick(item.city.id) },
                            onRemove = { onIntent(CitiesIntent.Remove(item.city.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityCard(
    item: CityWithWeather,
    unit: TemperatureUnit,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val current = item.forecast?.current
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                current?.condition?.emoji(current.isDay) ?: "⏳",
                style = MaterialTheme.typography.displaySmall,
            )
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(item.city.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    current?.condition?.label ?: item.city.region,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (current != null) {
                Text(unit.format(current.temperatureC), style = MaterialTheme.typography.headlineMedium)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remove ${item.city.name}")
            }
        }
    }
}
