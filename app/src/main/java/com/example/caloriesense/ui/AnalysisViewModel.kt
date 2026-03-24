package com.example.caloriesense.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.caloriesense.data.remote.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val result: String) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val geminiService: GeminiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("Tiếng Việt")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
    }

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = AnalysisState.Loading
            try {
                val result = geminiService.analyzeFoodImage(bitmap, _selectedLanguage.value)
                _uiState.value = AnalysisState.Success(result)
            } catch (e: Exception) {
                _uiState.value = AnalysisState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = AnalysisState.Idle
    }
}
