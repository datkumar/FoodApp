package com.phoenix.tech.foodapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.stageNewImage(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 25.dp, bottom = 100.dp), // Consistent padding prevents layout shift
        ) {
            // PROFILE PIC (editable)
            ProfileImageHeader(
                pfpUrl = if (viewModel.isEditMode) viewModel.editedProfilePic else userData?.profilePicture,
                isEditMode = viewModel.isEditMode,
                onEditClick = onRequestCameraPermission
            )
            Spacer(modifier = Modifier.height(28.dp))

            // USERNAME (editable)
            ProfileTextField(
                label = "Username",
                isEditable = viewModel.isEditMode,
                value = if (viewModel.isEditMode) viewModel.editedUsername else (userData?.userName ?: ""),
                onValueChange = { viewModel.editedUsername = it },
                isError = viewModel.isEditMode && !viewModel.isUsernameValid,
                supportingText = if (viewModel.isEditMode && !viewModel.isUsernameValid) "Minimum 6 characters and no symbols" else null,
                leadingIcon = painterResource(R.drawable.ic_user)
            )

            // PHONE NO. (editable)
            ProfileTextField(
                label = "Phone Number",
                isEditable = viewModel.isEditMode,
                value = if (viewModel.isEditMode) viewModel.editedPhone else (userData?.mobileNumber ?: ""),
                onValueChange = { viewModel.editedPhone = it },
                isError = viewModel.isEditMode && !viewModel.isPhoneValid,
                supportingText = if (viewModel.isEditMode && !viewModel.isPhoneValid)
                    "Enter 10-digit number starting with 6 or above" else null,
                leadingIcon = painterResource(R.drawable.ic_call),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            // EMAIL (read-only)
            ProfileTextField(
                label = "Email Address",
                isEditable = false, // Always locked
                value = userData?.email ?: "",
                onValueChange = {},
                leadingIcon = painterResource(R.drawable.ic_email_outlined)
            )

            // ROLE (read-only)
            ProfileTextField(
                label = "Account Role",
                isEditable = false,
                value = userData?.roleName ?: "User",
                onValueChange = {},
                leadingIcon = painterResource(R.drawable.ic_person_shield)
            )

            // Save & Cancel Buttons in Edit Mode
            AnimatedVisibility(
                visible = viewModel.isEditMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelEditing() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            enabled = viewModel.canSubmit,
                            onClick = { viewModel.saveChanges() },
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Save Profile")
                        }
                    }
                }
            }
        }

        // FAB visible only when NOT in edit mode
        AnimatedVisibility(
            visible = !viewModel.isEditMode,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 40.dp) // Sits above bottom navbar
        ) {
            ExtendedFloatingActionButton(
                onClick = { userData?.let { viewModel.startEditing(it) } },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit Profile",
                    )
                },
                text = { Text("Edit Profile") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}


@Composable
private fun ProfileTextField(
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
//            readOnly = !isEditable,
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
                // Dimmed states for locked/disabled fields
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        )
    }
}


@Composable
private fun ProfileImageHeader(
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
