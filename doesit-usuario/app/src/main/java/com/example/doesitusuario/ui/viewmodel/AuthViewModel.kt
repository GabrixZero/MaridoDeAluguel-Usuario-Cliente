package com.example.doesitusuario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doesitusuario.data.model.UserRegisterProfileDTO
import com.example.doesitusuario.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _successMessage = MutableStateFlow("")
    val successMessage: StateFlow<String> = _successMessage

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            repository.login(email, pass).fold(
                onSuccess = { onSuccess() },
                onFailure = { _errorMessage.value = it.message ?: "Erro ao entrar" }
            )
            _isLoading.value = false
        }
    }

    fun register(dto: UserRegisterProfileDTO, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            repository.register(dto).fold(
                onSuccess = { onSuccess(it) },
                onFailure = { _errorMessage.value = it.message ?: "Erro ao cadastrar" }
            )
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = ""
        _successMessage.value = ""
    }
}
