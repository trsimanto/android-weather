package dev.towhid.weather.ui.forecast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import dev.towhid.weather.domain.model.DailyForecast
import dev.towhid.weather.domain.model.Forecast
import dev.towhid.weather.domain.model.HourlyForecast
import dev.towhid.weather.domain.model.TemperatureUnit
import dev.towhid.weather.ui.components.EmptyState
import dev.towhid.weather.ui.components.OfflineBanner
import dev.towhid.weather.ui.components.formatUpdated
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ForecastScreen(onBack: () -> Unit, viewModel: ForecastViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ForecastContent(state, viewModel::onIntent, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastContent(state: ForecastUiState, onIntent: (ForecastIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.city?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onIntent(ForecastIntent.ToggleUnit) }) { Text(state.unit.symbol) }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(ForecastIntent.Refresh) },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            val forecast = state.forecast
            when {
                forecast != null -> ForecastBody(forecast, state.unit, state.showOfflineBanner)
                state.showError -> EmptyState("📡", "No data yet", "Connect to the internet and pull to refresh.")
                else -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ForecastBody(forecast: Forecast, unit: TemperatureUnit, offline: Boolean) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (offline) item { OfflineBanner() }
        item { CurrentConditions(forecast, unit) }
        item {
            SectionTitle("Next 24 hours")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(forecast.next24Hours, key = { it.time.toString() }) { HourCell(it, unit) }
            }
        }
        item { SectionTitle("7 days") }
        items(forecast.daily, key = { it.date.toString() }) { DayRow(it, unit) }
    }
}

@Composable
private fun CurrentConditions(forecast: Forecast, unit: TemperatureUnit) {
    val c = forecast.current
    Column(
        Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(c.condition.emoji(c.isDay), style = MaterialTheme.typography.displayLarge)
        Text(unit.format(c.temperatureC), style = MaterialTheme.typography.displayMedium)
        Text(c.condition.label, style = MaterialTheme.typography.titleMedium)
        Text(
            "Feels like ${unit.format(c.feelsLikeC)} · Humidity ${c.humidityPercent}% · Wind ${c.windKmh.roundToInt()} km/h",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
        )
        Text(
            formatUpdated(forecast.updatedAtMillis),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
}

private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
private fun HourCell(hour: HourlyForecast, unit: TemperatureUnit) {
    Card {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(hourFormatter.format(hour.time), style = MaterialTheme.typography.labelMedium)
            Text(hour.condition.emoji(isDay = hour.time.hour in 6..18), style = MaterialTheme.typography.titleLarge)
            Text(unit.format(hour.temperatureC), style = MaterialTheme.typography.titleMedium)
            if (hour.precipitationChance > 0) {
                Text("💧${hour.precipitationChance}%", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DayRow(day: DailyForecast, unit: TemperatureUnit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            modifier = Modifier.width(56.dp),
        )
        Text(day.condition.emoji(isDay = true), style = MaterialTheme.typography.titleLarge)
        Text(
            day.condition.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
        )
        if (day.precipitationChance > 0) {
            Text("💧${day.precipitationChance}%", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
        }
        Text("${unit.format(day.minC)} / ${unit.format(day.maxC)}")
    }
}
