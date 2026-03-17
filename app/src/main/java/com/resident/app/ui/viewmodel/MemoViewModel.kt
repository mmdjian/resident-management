package com.resident.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resident.app.data.entity.Memo
import com.resident.app.data.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoViewModel @Inject constructor(
    private val repository: MemoRepository
) : ViewModel() {

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private val _activeMemos = MutableStateFlow<List<Memo>>(emptyList())
    val activeMemos: StateFlow<List<Memo>> = _activeMemos.asStateFlow()

    private val _immediateMemos = MutableStateFlow<List<Memo>>(emptyList())
    val immediateMemos: StateFlow<List<Memo>> = _immediateMemos.asStateFlow()

    private val _dueMemos = MutableStateFlow<List<Memo>>(emptyList())
    val dueMemos: StateFlow<List<Memo>> = _dueMemos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadMemos()
        loadActiveMemos()
        loadImmediateMemos()
        checkDueMemos()
    }

    private fun loadMemos() {
        viewModelScope.launch {
            repository.getAllMemos().collect { list ->
                _memos.value = list
            }
        }
    }

    private fun loadActiveMemos() {
        viewModelScope.launch {
            repository.getActiveMemos().collect { list ->
                _activeMemos.value = list
            }
        }
    }

    private fun loadImmediateMemos() {
        viewModelScope.launch {
            repository.getImmediateMemos().collect { list ->
                _immediateMemos.value = list
            }
        }
    }

    fun checkDueMemos() {
        viewModelScope.launch {
            val due = repository.getDueMemos()
            _dueMemos.value = due
        }
    }

    suspend fun getMemoById(id: Long): Memo? {
        return repository.getMemoById(id)
    }

    fun insertMemo(title: String, content: String, isImmediate: Boolean, remindTime: Long = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val memo = Memo(
                    title = title,
                    content = content,
                    isImmediate = isImmediate,
                    remindTime = remindTime,
                    isCompleted = false
                )
                repository.insertMemo(memo)
                _message.value = if (isImmediate) "备忘已创建，打开应用时会提醒您" else "备忘已创建"
            } catch (e: Exception) {
                _message.value = "创建失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun updateMemo(memo: Memo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateMemo(memo.copy(updatedAt = System.currentTimeMillis()))
                _message.value = "更新成功"
            } catch (e: Exception) {
                _message.value = "更新失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun toggleMemoComplete(memo: Memo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateMemo(memo.copy(isCompleted = !memo.isCompleted, updatedAt = System.currentTimeMillis()))
                _message.value = if (!memo.isCompleted) "已完成" else "已恢复为未完成"
            } catch (e: Exception) {
                _message.value = "操作失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun deleteMemo(memo: Memo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteMemo(memo)
                _message.value = "删除成功"
            } catch (e: Exception) {
                _message.value = "删除失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun deleteMemoById(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteMemoById(id)
                _message.value = "删除成功"
            } catch (e: Exception) {
                _message.value = "删除失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun deleteAllMemos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteAllMemos()
                _message.value = "已清空所有备忘"
            } catch (e: Exception) {
                _message.value = "清空失败: ${e.message}"
            } finally { _isLoading.value = false }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
