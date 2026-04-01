package com.phoenix.tech.foodapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.model.RestoDetailsRestoInfo
import com.phoenix.tech.foodapp.data.model.SampleRestoDetails
import com.phoenix.tech.foodapp.ui.theme.AppTheme
import com.phoenix.tech.foodapp.ui.theme.getRatingColor

@Composable
fun RestaurantInfoHeader(
    restoInfo: RestoDetailsRestoInfo
) {
    val ratingColor = getRatingColor(restoInfo.avgRating)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restoInfo.name,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = restoInfo.cuisines.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${restoInfo.locality}, ${restoInfo.areaName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Rating Box
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 3.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star),
                            contentDescription = null,
                            tint = ratingColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = " ${restoInfo.avgRating}",
                            style = MaterialTheme.typography.titleMedium,
                            color = ratingColor
                        )
                    }
                    Text(
                        text = restoInfo.totalRatingsString,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_timer),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = " ${restoInfo.sla.slaString}",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = restoInfo.sla.lastMileTravelString ?: "Closeby",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun RestaurantInfoHeaderPreview() {
    val context = LocalContext.current
    val restoInfo = remember { SampleRestoDetails.sampleRestoDetailsRestoInfo(context) }
    AppTheme {
        RestaurantInfoHeader(
            restoInfo = restoInfo
        )
    }
}