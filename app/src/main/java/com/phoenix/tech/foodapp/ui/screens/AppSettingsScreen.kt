package com.phoenix.tech.foodapp.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phoenix.tech.foodapp.BuildConfig
import com.phoenix.tech.foodapp.ui.state.AppSettingsViewModel
import com.phoenix.tech.foodapp.ui.theme.AppTextFieldColors


@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel,
    onLogoutNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()

    ) {
        // Logout Confirmation Popup
        if (showLogoutConfirmDialog) {
            AlertDialog(
                title = {
                    Text(
                        "Confirm Logout",
                        style = MaterialTheme.typography.displayMedium
                    )
                },
                text = { Text("Are you sure you want to log out?") },
                onDismissRequest = { showLogoutConfirmDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutConfirmDialog = false // Dismiss dialog first
                            // Perform Logout via ViewModel
                            viewModel.performLogout(onLogoutSuccess = onLogoutNav)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showLogoutConfirmDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Cancel")
                    }
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Build Info",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 18.dp)
        )
        BuildInfoTable()
        Spacer(Modifier.height(30.dp))
        Button(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppTextFieldColors.Error),
            onClick = { showLogoutConfirmDialog = true }
        ) {
            Text(
                "Logout",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}


@Composable
private fun BuildInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun BuildInfoTable() {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        BuildInfoRow(
            label = "Version Name",
            value = BuildConfig.VERSION_NAME
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        BuildInfoRow(
            label = "Version Code",
            value = BuildConfig.VERSION_CODE.toString()
        )
    }
}