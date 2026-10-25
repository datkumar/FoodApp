package com.example.tech.foodapp.ui.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech.foodapp.data.model.UserData
import com.example.tech.foodapp.data.repository.SessionManager
import com.example.tech.foodapp.utils.ProfileImageUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModelFactory(
    private val sessionManager: SessionManager,
    private val applicationContext: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(sessionManager, applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val applicationContext: Context
) : ViewModel() {
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
    var editedProfilePic by mutableStateOf<String?>(null)
        private set
//    var stagedImageFile by mutableStateOf<File?>(null)
//        private set

    fun startEditing(currentUser: UserData) {
        editedUsername = currentUser.userName
        editedPhone = currentUser.mobileNumber
        editedProfilePic = currentUser.profilePicture
        ProfileImageUtils.getStagedFile(applicationContext).delete() // Clear old cache if any
        isEditMode = true
    }

    fun cancelEditing() {
        // Delete temporary staged image if any
        ProfileImageUtils.getStagedFile(applicationContext).delete()

        // Reset fields back to current user data defaults
        userData.value?.let { current ->
            editedUsername = current.userName
            editedPhone = current.mobileNumber
            editedProfilePic = current.profilePicture
        }

        isEditMode = false

//        ProfileImageUtils.getStagedFile(applicationContext).delete() // Cleanup temp file
//        editedProfilePic = null
//        isEditMode = false
    }

    fun stageNewImage(imagePath: String) {
        editedProfilePic = imagePath
    }

    // Input Fields Validators
    val isUsernameValid get() = editedUsername.matches(Regex("^[a-zA-Z0-9_]{6,}$"))
    val isPhoneValid get() = editedPhone.matches(Regex("[6-9]\\d{9}"))
    val canSubmit get() = isUsernameValid && isPhoneValid

    fun saveChanges() {
        viewModelScope.launch {
            userData.value?.let { current ->
                var finalPicPath = current.profilePicture

                // Use ProfileImageUtils to promote staged file to permanent storage
                val stagedFile = ProfileImageUtils.getStagedFile(applicationContext)
                if (stagedFile.exists()) {
                    val permanentFile = ProfileImageUtils.getPermanentFile(applicationContext)
                    stagedFile.copyTo(permanentFile, overwrite = true)
                    stagedFile.delete() // Clean up cache
                    finalPicPath = permanentFile.absolutePath
                }

                val updated = current.copy(
                    userName = editedUsername,
                    mobileNumber = editedPhone,
                    profilePicture = finalPicPath
                )

                sessionManager.saveUserInfo(updated)
                isEditMode = false
            }
        }
    }
}

