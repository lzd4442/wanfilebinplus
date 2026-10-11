package com.wanfile.binplus.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wanfile.binplus.data.converter.BinConverter
import com.wanfile.binplus.data.converter.ConvertResult
import com.wanfile.binplus.data.model.ConvertDirection
import com.wanfile.binplus.data.model.ConvertMode
import com.wanfile.binplus.data.model.ConvertRecord
import com.wanfile.binplus.data.local.ConvertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val mode: ConvertMode = ConvertMode.RAW,
    val direction: ConvertDirection = ConvertDirection.TO_BIN,
    val key: String = "90", // 0x5A
    val selectedFiles: List<Uri> = emptyList(),
    val isConverting: Boolean = false,
    val progress: Int = 0,
    val message: String? = null,
    val lastOutputPath: String? = null,
    val lastOutputName: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val converter: BinConverter,
    private val repository: ConvertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun setMode(mode: ConvertMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun setDirection(direction: ConvertDirection) {
        _uiState.update { it.copy(direction = direction) }
    }

    fun setKey(key: String) {
        _uiState.update { it.copy(key = key.filter { c -> c in '0'..'9' }.take(3)) }
    }

    fun addFiles(uris: List<Uri>) {
        _uiState.update { it.copy(selectedFiles = it.selectedFiles + uris) }
    }

    fun removeFile(uri: Uri) {
        _uiState.update { it.copy(selectedFiles = it.selectedFiles - uri) }
    }

    fun clearFiles() {
        _uiState.update { it.copy(selectedFiles = emptyList()) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, lastOutputPath = null, lastOutputName = null) }
    }

    fun startConvert() {
        val state = _uiState.value
        if (state.selectedFiles.isEmpty()) {
            _uiState.update { it.copy(message = "请先选择文件") }
            return
        }

        val keyByte = state.key.toIntOrNull()?.toByte() ?: run {
            _uiState.update { it.copy(message = "密钥需为 0-255 的整数") }
            return
        }

        _uiState.update { it.copy(isConverting = true, progress = 0, message = null) }

        viewModelScope.launch {
            state.selectedFiles.forEach { uri ->
                converter.convert(uri, state.mode, state.direction, keyByte)
                    .collect { result ->
                        when (result) {
                            is ConvertResult.Starting -> {
                                // 可在此处显示 toast
                            }
                            is ConvertResult.Progress -> {
                                _uiState.update { it.copy(progress = result.percent) }
                            }
                            is ConvertResult.Success -> {
                                // 保存记录
                                val record = ConvertRecord(
                                    originalName = uri.lastPathSegment ?: "unknown",
                                    outputName = result.outputPath.substringAfterLast('/'),
                                    originalPath = uri.toString(),
                                    outputPath = result.outputPath,
                                    sizeBytes = result.outputSize,
                                    mode = state.mode,
                                    direction = state.direction
                                )
                                repository.saveRecord(record)

                                _uiState.update {
                                    it.copy(
                                        progress = 100,
                                        message = "转换完成：${record.outputName}",
                                        lastOutputPath = result.outputPath,
                                        lastOutputName = record.outputName
                                    )
                                }
                            }
                            is ConvertResult.Error -> {
                                _uiState.update { it.copy(message = result.message) }
                            }
                        }
                    }
            }
            _uiState.update { it.copy(isConverting = false, progress = 0) }
        }
    }
}
