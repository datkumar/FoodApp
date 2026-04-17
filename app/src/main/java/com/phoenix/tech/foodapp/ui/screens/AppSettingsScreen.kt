package com.phoenix.tech.foodapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phoenix.tech.foodapp.ui.state.AppSettingsViewModel

@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel,
    onLogoutNav: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()

    ) {
        Text(
            "Settings Screen",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 15.dp)
        )
        Button(
            onClick = {
                // We call the ViewModel here.
                // It clears the data and THEN runs our navigation callback.
                viewModel.performLogout(onLogoutSuccess = onLogoutNav)
            }
        ) {
            Text("Logout")
        }
    }
}