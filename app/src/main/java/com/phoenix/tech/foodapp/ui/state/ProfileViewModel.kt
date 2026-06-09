package com.phoenix.tech.foodapp.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.model.UserData
import com.phoenix.tech.foodapp.data.repository.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {

    // Collect the Flow from DataStore and convert to StateFlow for Compose
    val userData: StateFlow<UserData?> = sessionManager.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // UI Editing States
    var isEditMode by mutableStateOf(false)
    var editedUsername by mutableStateOf("")
    var editedPhone by mutableStateOf("")

    fun startEditing(currentUser: UserData) {
        editedUsername = currentUser.userName
        editedPhone = currentUser.mobileNumber
        isEditMode = true
    }

    fun cancelEditing() {
        isEditMode = false
    }

    // Validation
    val isUsernameValid get() = editedUsername.length >= 6
    val isPhoneValid get() = editedPhone.length == 10 && editedPhone.firstOrNull() in '6'..'9'
    val canSubmit get() = isUsernameValid && isPhoneValid

    fun saveChanges() {
        viewModelScope.launch {
            userData.value?.let { current ->
                val updated = current.copy(
                    userName = editedUsername,
                    mobileNumber = editedPhone
                )
                sessionManager.saveUserInfo(updated)
                isEditMode = false
            }
        }
    }
}

