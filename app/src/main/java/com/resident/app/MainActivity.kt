package com.resident.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.resident.app.data.export.ExcelExporter
import com.resident.app.ui.navigation.NavGraph
import com.resident.app.ui.theme.ResidentAppTheme
import com.resident.app.ui.viewmodel.ResidentViewModel
import com.resident.app.ui.viewmodel.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var excelExporter: ExcelExporter

    private val residentViewModel: ResidentViewModel by viewModels()
    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ResidentAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        viewModel = residentViewModel,
                        statisticsViewModel = statisticsViewModel,
                        excelExporter = excelExporter
                    )
                }
            }
        }
    }
}
