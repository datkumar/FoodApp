package com.example.tech.foodapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tech.foodapp.R
import com.example.tech.foodapp.data.model.RestoDetailsCategoryFoodItem
import com.example.tech.foodapp.data.model.mocks.SampleRestoDetails
import com.example.tech.foodapp.ui.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun FoodItemRow(
    item: RestoDetailsCategoryFoodItem,
    quantity: Int, // Received from parent
    onQuantityChange: (Int) -> Unit // Callback to trigger ViewModel update
) {
    val priceString: String = when {
        item.price != null -> (item.price / 100.0).roundToInt().toString()
        item.defaultPrice != null -> (item.defaultPrice / 100.0).roundToInt().toString()
        else -> "N/A"
    }

    // For Veg OR Non-Veg Indicator
    val iconColor = if (item.isVeg == 1) Color(0xFF228B22) else Color(0xFFB22222)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // --- LEFT COLUMN: INFO ---
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_square_dot),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .height(18.dp)
                        .padding(end = 5.dp)
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- RIGHT COLUMN: PRICE & ACTION ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Center the button/selector
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(90.dp)
//            horizontalAlignment = Alignment.End,
//            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "₹$priceString",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 5.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Dynamic Cart Action Area
            if (quantity == 0) {
                // Initial ADD Button
                Button(
                    onClick = { onQuantityChange(1) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "ADD",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                // Quantity Selector: - Qty +
                OutlinedCard(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Decrease Button
                        IconButton(
                            onClick = { onQuantityChange(quantity - 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                "-",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Current Quantity
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Increase Button
                        IconButton(
                            onClick = { onQuantityChange(quantity + 1) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                "+",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodItemRowPreview() {
    val context = LocalContext.current
    val foodItem = remember { SampleRestoDetails.sampleFoodItem(context) }
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            FoodItemRow(
                item = foodItem,
                quantity = 1,
                onQuantityChange = {}
            )
        }
    }
}