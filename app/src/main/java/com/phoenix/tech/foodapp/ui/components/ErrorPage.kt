package com.phoenix.tech.foodapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.phoenix.tech.foodapp.R

@Composable
fun ErrorPage(msg: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.error_msg),
            contentDescription = "Error illustration",
            modifier = Modifier.height(200.dp),
            contentScale = ContentScale.FillHeight,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            msg,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}