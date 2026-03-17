package com.resident.app.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resident.app.data.export.ExcelExporter
import com.resident.app.data.repository.ResidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: ResidentRepository,
    private val excelExporter: ExcelExporter
) : ViewModel() {

    private val _statistics = MutableStateFlow<ResidentRepository.Statistics?>(null)
    val statistics: StateFlow<ResidentRepository.Statistics?> = _statistics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastBackupDays = MutableStateFlow(0L)
    val lastBackupDays: StateFlow<Long> = _lastBackupDays.asStateFlow()

    init { loadStatistics() }

    fun loadStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _statistics.value = repository.getStatistics()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBackupInfo(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
        val lastBackup = prefs.getLong("last_backup_time", 0L)
        if (lastBackup == 0L) {
            _lastBackupDays.value = 999L
        } else {
            val diff = System.currentTimeMillis() - lastBackup
            _lastBackupDays.value = diff / (1000 * 60 * 60 * 24)
        }
    }

    fun doBackup(context: Context, residentViewModel: ResidentViewModel) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val all = repository.getAllResidentsList()
                val result = excelExporter.exportToExcel(all, "备份")
                if (result.isSuccess) {
                    val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putLong("last_backup_time", System.currentTimeMillis()).apply()
                    _lastBackupDays.value = 0L
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
