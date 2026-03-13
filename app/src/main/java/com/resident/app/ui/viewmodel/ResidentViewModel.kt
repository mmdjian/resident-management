package com.resident.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resident.app.data.entity.Resident
import com.resident.app.data.repository.ResidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResidentViewModel @Inject constructor(
    private val repository: ResidentRepository
) : ViewModel() {

    private val _residents = MutableStateFlow<List<Resident>>(emptyList())
    val residents: StateFlow<List<Resident>> = _residents.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadResidents()
    }

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
                repository.getAllResidents().collect { list ->
                    _residents.value = list
                }
            } else {
                repository.searchResidents(query).collect { list ->
                    _residents.value = list
                }
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
            } finally {
                _isLoading.value = false
            }
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
            } finally {
                _isLoading.value = false
            }
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
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
