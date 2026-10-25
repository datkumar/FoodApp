package com.example.tech.foodapp.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tech.foodapp.R
import com.example.tech.foodapp.data.model.RestoListingItemData
import com.example.tech.foodapp.utils.RESTO_IMG_BASE_URL
import com.example.tech.foodapp.utils.RestoSharedElemAnimateKeys


fun getRestoImgFullUrl(imgId: String): String {
    return "$RESTO_IMG_BASE_URL/$imgId"
}

private const val cardImgHeight = 200
private const val cardCornerRadius = 15

@Composable
fun RestaurantListingCard(
    restaurant: RestoListingItemData,
    onRestaurantClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val screenTransitionKeys = RestoSharedElemAnimateKeys(restaurant.id)

    val transition = animatedVisibilityScope.transition
    val top by transition.animateDp(label = "imgTop") { if (it == EnterExitState.Visible) 15.dp else 12.dp }
    val bottom by transition.animateDp(label = "imgBottom") { if (it == EnterExitState.Visible) 0.dp else 12.dp }

    with(sharedTransitionScope) {
        Box(modifier) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRestaurantClick() },
                shape = RoundedCornerShape(cardCornerRadius.dp),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column {
                    // RESTAURANT IMAGE
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(getRestoImgFullUrl(restaurant.cloudinaryImageId))
                            .crossfade(true)
                            .crossfade(200)
                            .build(),
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.FillWidth,
                        placeholder = painterResource(R.drawable.resto_placeholder),
                        error = painterResource(R.drawable.resto_placeholder),
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(
                                    key = screenTransitionKeys.img // Pass same key in next screen component (Header Card)
                                ),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .clip(RoundedCornerShape(top, top, bottom, bottom))
                            .fillMaxWidth()
                            .height(cardImgHeight.dp)
                    )

                    // RESTAURANT DETAILS BELOW IMAGE
                    Column(
                        modifier = Modifier.padding(
                            top = 10.dp, bottom = 15.dp, start = 12.dp, end = 12.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = restaurant.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = screenTransitionKeys.restoName
                                        ),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                    .weight(1f)
                            )
                            // RATING
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(
                                    text = restaurant.avgRating.toString(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                RatingFractionalStar(restaurant.avgRating)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cuisines
                            Text(
                                text = restaurant.cuisines.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            // SLA Distance
                            Text(
                                text = restaurant.sla.lastMileTravelString,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(end = 10.dp)
                            )
                        }
                    }
                }
            }
            // SLA Time Overlay
            Text(
                text = restaurant.sla.slaString,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 5.dp, end = 5.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}


@Composable
fun RatingFractionalStar(
    rating: Double,
    modifier: Modifier = Modifier,
    fillColor: Color = Color(0xFFFFB300),
    backgroundColor: Color = Color.LightGray,
    starSize: Dp = 22.dp
) {
    // 4.2 out of 5.0 = 0.84 (84% of the star is yellow)
    val fraction = (rating / 5.0).toFloat().coerceIn(0f, 1f)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Empty Background
        Icon(
            painter = painterResource(id = R.drawable.ic_star),
            contentDescription = null,
            tint = backgroundColor,
            modifier = Modifier.size(starSize)
        )

        // Exact Decimal Fill
        Icon(
            painter = painterResource(id = R.drawable.ic_star),
            contentDescription = null,
            tint = fillColor,
            modifier = Modifier
                .size(starSize)
                .clip(GenericShape { size, _ ->
                    // This creates a rectangle from left to the fraction of the width
                    addRect(Rect(Offset.Zero, Size(size.width * fraction, size.height)))
                })
        )
    }
}
