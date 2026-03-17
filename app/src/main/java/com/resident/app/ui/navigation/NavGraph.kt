package com.resident.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.resident.app.data.export.ExcelExporter
import com.resident.app.ui.screens.AddEditResidentScreen
import com.resident.app.ui.screens.ExportScreen
import com.resident.app.ui.screens.LoginScreen
import com.resident.app.ui.screens.ResidentListScreen
import com.resident.app.ui.screens.StatisticsScreen
import com.resident.app.ui.viewmodel.ResidentViewModel
import com.resident.app.ui.viewmodel.StatisticsViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ResidentList : Screen("resident_list")
    object AddResident : Screen("add_resident")
    object EditResident : Screen("edit_resident/{residentId}") {
        fun createRoute(residentId: Long) = "edit_resident/$residentId"
    }
    object Statistics : Screen("statistics")
    object Export : Screen("export")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ResidentViewModel,
    statisticsViewModel: StatisticsViewModel,
    excelExporter: ExcelExporter
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ResidentList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ResidentList.route) {
            ResidentListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Screen.AddResident.route) },
                onEditClick = { resident ->
                    navController.navigate(Screen.EditResident.createRoute(resident.id))
                },
                onStatisticsClick = { navController.navigate(Screen.Statistics.route) },
                onExportClick = { navController.navigate(Screen.Export.route) }
            )
        }

        composable(Screen.AddResident.route) {
            AddEditResidentScreen(
                viewModel = viewModel,
                resident = null,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditResident.route,
            arguments = listOf(navArgument("residentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val residentId = backStackEntry.arguments?.getLong("residentId") ?: return@composable
            val residentList by viewModel.residents.collectAsState(initial = emptyList())
            val resident = residentList.find { it.id == residentId }
            AddEditResidentScreen(
                viewModel = viewModel,
                resident = resident,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                residentViewModel = viewModel,
                onBack = { navController.popBackStack() },
                onExportClick = { navController.navigate(Screen.Export.route) }
            )
        }

        composable(Screen.Export.route) {
            ExportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
