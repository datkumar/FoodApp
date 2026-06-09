package com.phoenix.tech.foodapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.ui.state.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onRequestCameraPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userData by viewModel.userData.collectAsState()
    val scrollState = rememberScrollState()


    Column(
        modifier = modifier
            .fillMaxSize()
//            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Edit/Cancel Toggle Button
        TextButton(onClick = {
            if (viewModel.isEditMode) viewModel.cancelEditing()
            else userData?.let { viewModel.startEditing(it) }
        }) {
            Text(if (viewModel.isEditMode) "Cancel" else "Edit")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Profile Picture Section ---
        ProfileImageHeader(
            pfpUrl = userData?.profilePicture,
            isEditMode = viewModel.isEditMode,
            onEditClick = onRequestCameraPermission
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Information Fields ---

        // Editable: Username
        ProfileTextField(
            label = "Username",
            value = if (viewModel.isEditMode) {
                viewModel.editedUsername
            } else {
                userData?.userName ?: ""
            },
            onValueChange = { viewModel.editedUsername = it },
            isEditable = viewModel.isEditMode,
            isError = viewModel.isEditMode && !viewModel.isUsernameValid,
            supportingText = if (viewModel.isEditMode && !viewModel.isUsernameValid)
                "Minimum 6 characters required" else null,
            leadingIcon = painterResource(R.drawable.ic_user)
        )

        // Editable: Phone
        ProfileTextField(
            label = "Phone Number",
            value = if (viewModel.isEditMode) viewModel.editedPhone else (userData?.mobileNumber
                ?: ""),
            onValueChange = { if (it.length <= 10) viewModel.editedPhone = it },
            isEditable = viewModel.isEditMode,
            isError = viewModel.isEditMode && !viewModel.isPhoneValid,
            supportingText = if (viewModel.isEditMode && !viewModel.isPhoneValid)
                "Enter valid 10-digit number (starts with 6-9)" else null,
            leadingIcon = painterResource(R.drawable.ic_call),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // Read-Only: Email
        ProfileTextField(
            label = "Email Address",
            value = userData?.email ?: "",
            onValueChange = {},
            isEditable = false, // Always locked
            leadingIcon = painterResource(R.drawable.ic_email_outlined)
        )

        // Read-Only: Role
        ProfileTextField(
            label = "Account Role",
            value = userData?.roleName ?: "User",
            onValueChange = {},
            isEditable = true,
            leadingIcon = painterResource(R.drawable.ic_person_shield)
        )

        Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom bar

        // Show Submit button only in Edit Mode
        AnimatedVisibility(
            visible = viewModel.isEditMode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Button(
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                onClick = { viewModel.saveChanges() },
                enabled = viewModel.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Profile Settings")
            }
        }
    }
}


@Composable
fun ProfileImageHeader(
    pfpUrl: String?,
    isEditMode: Boolean,
    onEditClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.BottomEnd
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(pfpUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            // Fallback strategy: Error and Placeholder
            error = painterResource(id = R.drawable.ic_user),
            placeholder = painterResource(id = R.drawable.ic_user),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                )
        )

        if (isEditMode) {
            FilledIconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (-4).dp, y = (-4).dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "Change Picture",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditable: Boolean,
    leadingIcon: Painter,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            enabled = isEditable,
            readOnly = !isEditable,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            supportingText = supportingText?.let { { Text(it) } },
            keyboardOptions = keyboardOptions,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        )
    }
}