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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phoenix.tech.foodapp.BuildConfig
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
            "Build Info",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(vertical = 15.dp)
        )
        BuildInfo()
        Spacer(Modifier.height(15.dp))
        Button(
            onClick = {
                // We call the ViewModel here.
                viewModel.performLogout(onLogoutSuccess = onLogoutNav)
            }
        ) {
            Text("Logout")
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
private fun BuildInfo() {
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