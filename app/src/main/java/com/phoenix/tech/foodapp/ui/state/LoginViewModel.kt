package com.phoenix.tech.foodapp.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.repository.AuthRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LoginViewModel(application: Application) : AndroidViewModel(application) {
    // Manually wiring dependencies (later use Hilt)
    private val sessionManager = SessionManager(application)
    private val repository = AuthRepository(sessionManager = sessionManager)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, pswd: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = repository.login(email, pswd)
            _uiState.value = result.fold(
                onSuccess = { token -> LoginUiState.Success(token) },
                onFailure = { exception ->
                    LoginUiState.Error(
                        exception.message ?: "Unknown Error"
                    )
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