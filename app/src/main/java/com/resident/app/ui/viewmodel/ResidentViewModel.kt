package com.resident.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resident.app.data.entity.Resident
import com.resident.app.data.export.ExcelExporter
import com.resident.app.data.import_excel.ExcelImporter
import com.resident.app.data.repository.ResidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentViewModel @Inject constructor(
    private val repository: ResidentRepository,
    private val excelExporter: ExcelExporter,
    private val excelImporter: ExcelImporter
) : ViewModel() {

    private val _residents = MutableStateFlow<List<Resident>>(emptyList())
    val residents: StateFlow<List<Resident>> = _residents.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init { loadResidents() }

    private fun loadResidents() {
        viewModelScope.launch {
            repository.getAllResidents().collect { list ->
                _residents.value = list
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                repository.getAllResidents().collect { _residents.value = it }
            } else {
                repository.searchResidents(query).collect { _residents.value = it }
            }
        }
    }

    fun insertResident(resident: Resident) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.insertResident(resident)
                _message.value = "保存成功"
            } catch (e: Exception) {
                _message.value = "保存失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun updateResident(resident: Resident) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateResident(resident.copy(updatedAt = System.currentTimeMillis()))
                _message.value = "更新成功"
            } catch (e: Exception) {
                _message.value = "更新失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun deleteResident(resident: Resident) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteResident(resident)
                _message.value = "删除成功"
            } catch (e: Exception) {
                _message.value = "删除失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    // 按条件筛选并导出 Excel
    fun exportFiltered(
        genderFilter: String = "",
        educationFilter: String = "",
        customFieldFilter: Pair<String, String>? = null  // key to value
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val all = repository.getAllResidentsList()
                val filtered = all.filter { r ->
                    (genderFilter.isEmpty() || r.gender == genderFilter) &&
                    (educationFilter.isEmpty() || r.education == educationFilter) &&
                    (customFieldFilter == null || r.customFields[customFieldFilter.first] == customFieldFilter.second)
                }
                val desc = buildString {
                    if (genderFilter.isNotEmpty()) append(genderFilter)
                    if (educationFilter.isNotEmpty()) { if (isNotEmpty()) append("_"); append(educationFilter) }
                    if (customFieldFilter != null) { if (isNotEmpty()) append("_"); append("${customFieldFilter.first}${customFieldFilter.second}") }
                    if (isEmpty()) append("全部")
                }
                val result = excelExporter.exportToExcel(filtered, desc)
                if (result.isSuccess) {
                    _message.value = "导出成功：${result.getOrNull()}"
                } else {
                    _message.value = "导出失败"
                }
            } catch (e: Exception) {
                _message.value = "导出失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    // 从 Excel 导入居民数据
    fun importFromExcel(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = excelImporter.importFromExcel(uri)
                if (result.errorMsg.isNotEmpty()) {
                    _message.value = "导入失败：${result.errorMsg}"
                } else if (result.residents.isEmpty()) {
                    _message.value = "没有找到可导入的数据，请检查文件格式"
                } else {
                    result.residents.forEach { repository.insertResident(it) }
                    val msg = buildString {
                        append("导入成功！共导入 ${result.success} 条记录")
                        if (result.failed > 0) append("，${result.failed} 条因姓名为空已跳过")
                    }
                    _message.value = msg
                }
            } catch (e: Exception) {
                _message.value = "导入失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
