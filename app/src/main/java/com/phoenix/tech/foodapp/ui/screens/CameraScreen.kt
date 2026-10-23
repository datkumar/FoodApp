package com.phoenix.tech.foodapp.ui.screens


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.ui.state.CameraViewModel
import java.io.File

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onPhotoConfirmed: (File) -> Unit,
    onDone: () -> Unit,
    startAsFront: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var capturedFile by remember { mutableStateOf<File?>(null) }
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    var isPreviewMode by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(lifecycleOwner, hasCameraPermission) {
        if (hasCameraPermission) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner, startAsFront)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- Viewfinder / Captured Preview Area ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (!hasCameraPermission) {
                Text(
                    text = "Camera permission is required",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                if (!isPreviewMode) {
                    surfaceRequest?.let { request ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clipToBounds()
                        ) {
                            CameraXViewfinder(
                                surfaceRequest = request,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = capturedFile,
                        contentDescription = "Captured Image Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // EXIT Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            OutlinedButton(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Exit")
            }
        }

        // ACTION BUTTONS AT BOTTOM
        if (hasCameraPermission) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 30.dp, vertical = 24.dp), // Clearance for bottom navbar
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bottom Left: Flip Camera or Retake
                if (!isPreviewMode) {
                    FloatingActionButton(
                        onClick = { viewModel.flipCamera(context.applicationContext, lifecycleOwner) },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera_flip),
                            contentDescription = "Flip Camera",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    FloatingActionButton(
                        onClick = {
                            capturedFile?.delete()
                            capturedFile = null
                            isPreviewMode = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ) {
                        Text(
                            text = "Retake",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                        )
                    }
                }

                // Bottom Right: Capture Photo or Confirm
                if (!isPreviewMode) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.capturePhoto(context) { file ->
                                capturedFile = file
                                isPreviewMode = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera_capture),
                            contentDescription = "Capture Photo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    FloatingActionButton(
                        onClick = {
                            capturedFile?.let { tempFile ->
                                onPhotoConfirmed(tempFile)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ) {
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }

}