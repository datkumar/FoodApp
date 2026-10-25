package com.example.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech.foodapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AppSettingsViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppSettingsViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AppSettingsViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun performLogout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logoutUser()
            onLogoutSuccess()
        }
    }
}