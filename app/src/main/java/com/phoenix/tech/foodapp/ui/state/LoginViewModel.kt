package com.phoenix.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class LoginViewModel(
    private val authRepository: AuthRepository // Injected via Factory
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, pswd: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            // Use the shared repository
            val result = authRepository.login(email, pswd)
            _uiState.value = result.fold(
                onSuccess = { token -> LoginUiState.Success(token) },
                onFailure = { exception ->
                    LoginUiState.Error(exception.message ?: "Unknown Error")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}


//class LoginViewModel(
//    private val repository: AuthRepository = AuthRepository(RetrofitClient.authApi)
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
//    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
//
//    fun login(email: String, pswd: String) {
//        viewModelScope.launch {
//            _uiState.value = LoginUiState.Loading
//            val result = repository.login(email, pswd)
//            _uiState.value = result.fold(
//                onSuccess = { token -> LoginUiState.Success(token) },
//                onFailure = { exception ->
//                    LoginUiState.Error(
//                        exception.message ?: "Unknown Error"
//                    )
//                }
//            )
//        }
//    }
//
//    fun resetState() {
//        _uiState.value = LoginUiState.Idle
//    }
//}