package com.example.doesitusuario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doesitusuario.data.model.NotificationDTO
import com.example.doesitusuario.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _notifications = MutableStateFlow<List<NotificationDTO>>(emptyList())
    val notifications: StateFlow<List<NotificationDTO>> = _notifications

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            repository.getNotifications().fold(
                onSuccess = { 
                    _notifications.value = it
                },
                onFailure = { 
                    _errorMessage.value = it.message ?: "Erro ao carregar notificações"
                }
            )
            _isLoading.value = false
        }
    }
}
