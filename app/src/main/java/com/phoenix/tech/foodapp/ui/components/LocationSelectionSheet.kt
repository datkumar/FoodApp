package com.phoenix.tech.foodapp.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.ui.state.LocationResult
import com.phoenix.tech.foodapp.ui.state.LocationViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionSheet(
    viewModel: LocationViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onQueryChange("")
            // If you want to clear results too, add a clearResults() function in VM
        }
    }

    val queryText by viewModel.query.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDetecting by viewModel.isDetecting.collectAsState()
    val context = LocalContext.current

    // Helper to check if permission is actually granted right now
    val isPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .fillMaxHeight(0.8f) // Keeps it from taking the whole screen
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 5.dp)
            ) {
                Text(
                    "Search Location",
                    style = MaterialTheme.typography.titleLarge
                )

                // Current Location
                TextButton(
                    enabled = !isDetecting, // Disable while device location being detected
                    onClick = {
                        scope.launch {
                            if (isPermissionGranted) {
                                // Pass forceRefresh = true to override any manual selection
                                val result = viewModel.resolveAndSaveLocation(forceRefresh = true)

                                when (result) {
                                    is LocationResult.Success -> {
                                        onDismiss() // Close the sheet/menu on success
                                    }

                                    is LocationResult.GpsDisabled -> {
                                        // User has permission, but the "Location" toggle in Android settings is OFF
                                        Toast.makeText(
                                            context,
                                            "GPS is turned off. Please enable Location in your Quick Settings.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    is LocationResult.Failure -> {
                                        // General timeout or provider error
                                        Toast.makeText(
                                            context,
                                            "Detection timed out. Using default fallback.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    else -> { /* PermissionDenied is handled by the outer 'if' */
                                    }
                                }
                            } else {
                                // Permission is NOT granted: Redirect to App Settings
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .apply {
                                            data = Uri.fromParts(
                                                "package", context.packageName, null
                                            )
                                        }
                                context.startActivity(intent)
                                Toast.makeText(
                                    context,
                                    "Please enable Location permission in Settings",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
//                    onClick = {
//                        if (isPermissionGranted) {
//                            // FIX: Use the scope to launch the suspend function
//                            scope.launch {
//                                // Pass forceRefresh = true to bypass the manual location guard
//                                val status = viewModel.resolveAndSaveLocation(forceRefresh = true)
//                                if (status == LocationResult.Success) {
//                                    // Check if a location was actually found or if it just returned the manual one
//                                    // Usually, if resolveAndSaveLocation returns true, it's safe to dismiss
//                                    onDismiss() // Close sheet only on success
//                                } else {
//                                    // This happens if Permissions are ON but GPS/Network providers are OFF
//                                    Toast.makeText(
//                                        context,
//                                        "Could not detect location. Please check if GPS Location is ON",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//                            }
//                        } else {
//                            // Prepare the intent to open THIS app's settings
//                            val intent = Intent(
//                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                            ).apply {
//                                data = Uri.fromParts("package", context.packageName, null)
//                            }
//                            // Launch the system activity
//                            context.startActivity(intent)
//                            // Inform the user WHY they are being moved
//                            Toast.makeText(
//                                context,
//                                "Please enable Location permission in App Settings",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    },
                    colors = ButtonDefaults.textButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(
                            painterResource(R.drawable.ic_location_current),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isDetecting) "Detecting..." else "Current Location",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            OutlinedTextField(
                label = {
                    Text("Enter your city or area name")
                },
                value = queryText,
                onValueChange = {
                    viewModel.onQueryChange(it)
                },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (queryText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.onQueryChange("")
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_close),
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(results) { result ->
                    ListItem(
                        headlineContent = {
                            Text(result.formattedName)
                        },
                        leadingContent = {
                            Icon(
                                painterResource(R.drawable.ic_location_pin),
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable {
                            viewModel.selectLocation(result)
                            onDismiss()
                        }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}