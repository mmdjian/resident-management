package com.resident.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.resident.app.data.entity.Resident
import com.resident.app.data.export.ExcelExporter
import com.resident.app.ui.screens.AddEditResidentScreen
import com.resident.app.ui.screens.ExportScreen
import com.resident.app.ui.screens.ImportScreen
import com.resident.app.ui.screens.LoginScreen
import com.resident.app.ui.screens.ResidentListScreen
import com.resident.app.ui.screens.StatisticsScreen
import com.resident.app.ui.viewmodel.ResidentViewModel
import com.resident.app.ui.viewmodel.StatisticsViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object ResidentList : Screen("resident_list") {
        fun createRoute(filter: String? = null) = if (filter != null) "resident_list?filter=$filter" else "resident_list"
    }
    object AddResident : Screen("add_resident")
    object EditResident : Screen("edit_resident/{residentId}") {
        fun createRoute(residentId: Long) = "edit_resident/$residentId"
    }
    object Statistics : Screen("statistics")
    object Export : Screen("export")
    object Import : Screen("import")
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

        composable(
            route = "resident_list?filter={filter}",
            arguments = listOf(navArgument("filter") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter")
            LaunchedEffect(filter) {
                if (filter != null) {
                    viewModel.applyFilter(filter)
                } else {
                    viewModel.clearFilter()
                }
            }
            ResidentListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Screen.AddResident.route) },
                onEditClick = { resident ->
                    navController.navigate(Screen.EditResident.createRoute(resident.id))
                },
                onStatisticsClick = { navController.navigate(Screen.Statistics.route) },
                onExportClick = { navController.navigate(Screen.Export.route) },
                onImportClick = { navController.navigate(Screen.Import.route) }
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
            var resident by remember { mutableStateOf<Resident?>(null) }
            LaunchedEffect(residentId) {
                resident = viewModel.getResidentById(residentId)
            }
            if (resident != null) {
                AddEditResidentScreen(
                    viewModel = viewModel,
                    resident = resident,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                residentViewModel = viewModel,
                onBack = { navController.popBackStack() },
                onExportClick = { navController.navigate(Screen.Export.route) },
                onFilterClick = { filter ->
                    navController.navigate(Screen.ResidentList.createRoute(filter))
                }
            )
        }

        composable(Screen.Export.route) {
            ExportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Import.route) {
            ImportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
