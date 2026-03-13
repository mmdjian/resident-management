package com.resident.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.resident.app.data.entity.Resident
import com.resident.app.data.export.ExcelExporter
import com.resident.app.ui.screens.AddEditResidentScreen
import com.resident.app.ui.screens.ResidentListScreen
import com.resident.app.ui.screens.StatisticsScreen
import com.resident.app.ui.viewmodel.ResidentViewModel
import com.resident.app.ui.viewmodel.StatisticsViewModel

sealed class Screen(val route: String) {
    object ResidentList : Screen("resident_list")
    object AddResident : Screen("add_resident")
    object EditResident : Screen("edit_resident/{residentId}") {
        fun createRoute(residentId: Long) = "edit_resident/$residentId"
    }
    object Statistics : Screen("statistics")
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
        startDestination = Screen.ResidentList.route
    ) {
        composable(Screen.ResidentList.route) {
            ResidentListScreen(
                viewModel = viewModel,
                onAddClick = {
                    navController.navigate(Screen.AddResident.route)
                },
                onEditClick = { resident ->
                    navController.navigate(Screen.EditResident.createRoute(resident.id))
                },
                onStatisticsClick = {
                    navController.navigate(Screen.Statistics.route)
                },
                onExportClick = {
                    // Excel 导出逻辑
                }
            )
        }

        composable(Screen.AddResident.route) {
            AddEditResidentScreen(
                viewModel = viewModel,
                resident = null,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditResident.route,
            arguments = listOf(navArgument("residentId") { type = NavType.LongType })
        ) {
            val residentId = it.arguments?.getLong("residentId") ?: return@composable
            // 这里需要从 ViewModel 获取居民信息
            AddEditResidentScreen(
                viewModel = viewModel,
                resident = null, // TODO: 从 ViewModel 获取
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
