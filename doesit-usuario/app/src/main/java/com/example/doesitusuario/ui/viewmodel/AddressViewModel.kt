package com.example.doesitusuario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doesitusuario.data.model.AddressDTO
import com.example.doesitusuario.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddressViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _addresses = MutableStateFlow<List<AddressDTO>>(emptyList())
    val addresses: StateFlow<List<AddressDTO>> = _addresses

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun loadAddresses() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            repository.getAddresses().fold(
                onSuccess = { 
                    _addresses.value = it
                },
                onFailure = { 
                    _errorMessage.value = it.message ?: "Erro ao carregar endereços"
                }
            )
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = ""
    }
}
