package com.phoenix.tech.foodapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.model.RestoListingItemData
import com.phoenix.tech.foodapp.data.model.SampleRestoListing
import com.phoenix.tech.foodapp.ui.theme.AppTheme


private fun getFullImgUrl(imgId: String): String {
    return "https://media-assets.swiggy.com/swiggy/image/upload/fl_lossy,f_auto,q_auto,w_660/$imgId"
}


@Composable
fun RestaurantCard(
    restaurant: RestoListingItemData,
    onRestaurantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRestaurantClick() },
        shape = RoundedCornerShape(13.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            AsyncImage(
                model = getFullImgUrl(restaurant.cloudinaryImageId),
                contentDescription = restaurant.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.FillWidth
            )
            CardBottomContent(restaurant)
        }
    }
}


@Composable
private fun CardBottomContent(restaurant: RestoListingItemData) {
    Column(
        modifier = Modifier.padding(
            top = 10.dp, bottom = 15.dp, start = 6.dp, end = 6.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)

            )
            Spacer(modifier = Modifier.width(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = restaurant.avgRating.toString(),
                    style = MaterialTheme.typography.titleSmall
                )
                Icon(
                    painterResource(R.drawable.ic_star),
                    "Rating", tint = Color(0xFFFFB300),
                    modifier = Modifier.size(22.dp)
                )

            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = restaurant.cuisines.joinToString(", "),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = restaurant.sla.slaString,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                // Delivery time is short, so we don't want it to shrink
                modifier = Modifier.wrapContentWidth()
            )
        }
    }
}

@Preview(showBackground = false, widthDp = 200)
@Composable
fun RestaurantCardPreview(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val restaurant = remember { SampleRestoListing.sampleRestoListingItem(context) }

    AppTheme(darkTheme = false) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = modifier
                .fillMaxWidth()
                .clickable { },
            shape = RoundedCornerShape(13.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(R.drawable.kfc),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.FillWidth,
                )
                CardBottomContent(restaurant)
            }
        }
    }
}
