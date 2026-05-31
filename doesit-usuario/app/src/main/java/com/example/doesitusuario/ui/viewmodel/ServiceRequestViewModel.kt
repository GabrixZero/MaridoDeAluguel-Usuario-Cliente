package com.example.doesitusuario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doesitusuario.data.model.ServiceRequestFormResponse
import com.example.doesitusuario.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceRequestViewModel(private val repository: ServiceRepository = ServiceRepository()) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _formData = MutableStateFlow<ServiceRequestFormResponse?>(null)
    val formData: StateFlow<ServiceRequestFormResponse?> = _formData

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun loadFormData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            repository.getServiceRequestForm().fold(
                onSuccess = { 
                    _formData.value = it
                },
                onFailure = { 
                    _errorMessage.value = it.message ?: "Erro ao carregar dados"
                }
            )
            _isLoading.value = false
        }
    }
}
