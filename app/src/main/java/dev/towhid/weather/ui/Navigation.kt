package dev.towhid.weather.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.towhid.weather.ui.cities.CitiesScreen
import dev.towhid.weather.ui.forecast.ForecastScreen
import dev.towhid.weather.ui.search.SearchScreen

object Routes {
    const val CITIES = "cities"
    const val SEARCH = "search"
    const val FORECAST = "forecast/{cityId}"
    const val ARG_CITY_ID = "cityId"
    fun forecast(cityId: Long) = "forecast/$cityId"
}

@Composable
fun WeatherNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.CITIES) {
        composable(Routes.CITIES) {
            CitiesScreen(
                onCityClick = { navController.navigate(Routes.forecast(it)) },
                onAddCity = { navController.navigate(Routes.SEARCH) },
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.FORECAST,
            arguments = listOf(navArgument(Routes.ARG_CITY_ID) { type = NavType.LongType }),
        ) {
            ForecastScreen(onBack = { navController.popBackStack() })
        }
    }
}
