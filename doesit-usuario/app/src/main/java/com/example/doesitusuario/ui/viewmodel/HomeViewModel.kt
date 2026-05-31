package com.example.doesitusuario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doesitusuario.data.network.SessionManager
import com.example.doesitusuario.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userName = MutableStateFlow(SessionManager.userName)
    val userName: StateFlow<String> = _userName

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCurrentUser().fold(
                onSuccess = { 
                    _userName.value = it
                    _error.value = ""
                },
                onFailure = { 
                    _error.value = it.message ?: "Erro ao atualizar dados"
                }
            )
            _isLoading.value = false
        }
    }
}
