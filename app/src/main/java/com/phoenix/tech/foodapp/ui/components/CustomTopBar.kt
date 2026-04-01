package com.phoenix.tech.foodapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.ui.theme.AppTheme
import com.phoenix.tech.foodapp.utils.trimOverflowingText


//const val topBarTag = "TOPBAR"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    title: String,
    isHomeRoute: Boolean,
    isCartRoute: Boolean,
    showBack: Boolean,
    cartCount: Int = 0,
    displayedLocation: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onLocationClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        title = {
            Text(
                trimOverflowingText(title, 20),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayMedium
            )
        },
        navigationIcon = {
            // Priority 1: Show Back Button if the config says so
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            // Priority 2: If no back button, and we are on Home, show Location Picker
            else if (isHomeRoute) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 2.dp)
                        .widthIn(min = 80.dp) // 👈 balance space
                ) {
                    Surface(
                        onClick = onLocationClick,
                        shape = RoundedCornerShape(40),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_location_pin),
                                contentDescription = "Location Pin",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                trimOverflowingText(displayedLocation, 20),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        actions = {
            if (!isCartRoute) {
                // Cart Icon with Badge
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Box(modifier = Modifier.padding(top = 6.dp, end = 6.dp)) {
                        BadgedBox(

                            badge = {
                                if (cartCount > 0) {
                                    val badgeText =
                                        if (cartCount >= 10) "9+" else cartCount.toString()
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        Text(
                                            badgeText,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cart),
                                contentDescription = "My Cart",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    )
}


@Preview
@Composable
fun CustomTopBarPreview() {
    AppTheme {
        CustomTopBar(
            title = "Zwiggy",
            isHomeRoute = true,
            isCartRoute = false,
            showBack = false,
            displayedLocation = "Pune, Maharashtra",
            onBackClick = { },
            onCartClick = {},
            onLocationClick = {},
        )
    }
}