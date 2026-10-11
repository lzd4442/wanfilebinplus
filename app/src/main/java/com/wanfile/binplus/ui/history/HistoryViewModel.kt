package com.wanfile.binplus.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanfile.binplus.data.model.ConvertRecord
import com.wanfile.binplus.data.local.ConvertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val records: List<ConvertRecord> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ConvertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        repository.observeHistory()
            .onEach { records ->
                _uiState.value = HistoryUiState(records = records)
            }
            .launchIn(viewModelScope)
    }

    fun delete(record: ConvertRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
