package com.example.tech.foodapp.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tech.foodapp.R
import com.example.tech.foodapp.ui.theme.getRatingColor
import com.example.tech.foodapp.utils.RestoSharedElemAnimateKeys


@Composable
fun RestaurantInfoHeader(
    restoId: String,
    restoName: String, imgId: String,
    avgRating: Double, totalRatingsString: String,
    slaTime: String, slaDist: String,
    cuisines: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val screenTransitionKeys = RestoSharedElemAnimateKeys(restoId)

    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(15.dp),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // RESTO IMAGE
                AsyncImage(
                    model = getRestoImgFullUrl(imgId),
                    contentDescription = "Restaurant Cover image",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(120.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(
                                key = screenTransitionKeys.img // Match the key like we had in Restaurant Card
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.width(12.dp))
                // RESTO INFO
                Column {
                    // TITLE (Restaurant Name)
                    Text(
                        text = restoName,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState(key = screenTransitionKeys.restoName),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                    )
                    Spacer(Modifier.height(5.dp))
                    // CUISINES
                    Text(
                        cuisines,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))

                    // RATINGS INFO ROW
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.skipToLookaheadSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .background(
                                    color = getRatingColor(avgRating),
                                    shape = CircleShape // Makes it a pill
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = avgRating.toString(),
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                                color = Color.White
                            )
                            Icon(
                                painter = painterResource(R.drawable.ic_star),
                                contentDescription = "Star icon",
                                modifier = Modifier.size(15.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        // Number of ratings
                        Text(
                            "$totalRatingsString ratings",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    // SLA Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.skipToLookaheadSize()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_timer),
                            contentDescription = "Timer icon",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 2.dp)
                        )
                        Text(
                            slaTime,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_moped),
                            contentDescription = "Moped icon",
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 2.dp)
                        )
                        Text(
                            slaDist,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }

                }
            }
        }
    }
}

//@Preview
//@Composable
//fun RestaurantInfoHeaderPreview() {
//    val context = LocalContext.current
//    val restoInfo = remember { SampleRestoDetails.sampleRestoDetailsRestoInfo(context) }
//    AppTheme {
//        RestaurantInfoHeader(
//            restoInfo = restoInfo
//        )
//    }
//}