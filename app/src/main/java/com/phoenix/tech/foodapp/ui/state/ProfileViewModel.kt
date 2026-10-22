package com.phoenix.tech.foodapp.ui.state

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.model.UserData
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.utils.ProfileImageUtils
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
    var editedProfilePic by mutableStateOf("") // Holds staged path temporarily

    fun startEditing(currentUser: UserData) {
        editedUsername = currentUser.userName
        editedPhone = currentUser.mobileNumber
        editedProfilePic = currentUser.profilePicture // Start with current pic
        isEditMode = true
    }

    fun cancelEditing() {
        // Discard staged file if it exists
        val stagedFile = ProfileImageUtils.getStagedFile(applicationContext)
        if (stagedFile.exists()) stagedFile.delete()

        isEditMode = false
    }

    fun stageNewImage(uri: Uri) {
        val stagedFile = ProfileImageUtils.getStagedFile(applicationContext)
        ProfileImageUtils.copyUriToFile(applicationContext, uri, stagedFile)
        editedProfilePic = stagedFile.absolutePath
    }
    
    // Input Fields Validators
    val isUsernameValid get() = editedUsername.matches(Regex("^[a-zA-Z0-9_]{6,}$"))
    val isPhoneValid get() = editedPhone.matches(Regex("[6-9]\\d{9}"))
    val canSubmit get() = isUsernameValid && isPhoneValid

    fun saveChanges() {
        viewModelScope.launch {
            userData.value?.let { current ->
                var finalPicPath = current.profilePicture
                // Commit staged file to permanent storage if a new one was staged
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

