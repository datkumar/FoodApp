package com.phoenix.tech.foodapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.local.entities.CartItemEntity
import com.phoenix.tech.foodapp.data.repository.CartRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.ui.state.CartViewModel
import com.phoenix.tech.foodapp.ui.state.CartViewModelFactory

@Composable
fun CartScreen(
    cartRepository: CartRepository,
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit,
    cartEmptyRedirect: () -> Unit
) {
    val factory = remember {
        CartViewModelFactory(cartRepository, sessionManager)
    }
    val viewModel: CartViewModel = viewModel(factory = factory)

    val groupedItems by viewModel.groupedItems.collectAsStateWithLifecycle()
    val totalBillState = viewModel.cartTotal.collectAsStateWithLifecycle()
    val totalBill = totalBillState.value

    if (groupedItems.isEmpty()) {
        EmptyCartView(
            onNavigateBack = onNavigateBack,
            cartEmptyRedirect = cartEmptyRedirect
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            groupedItems.forEach { (restaurant, items) ->
                // Restaurant Name Header
                item {
                    Column(
                        modifier = Modifier.padding(
                            top = 10.dp, bottom = 5.dp
                        )
                    ) {
                        Text(
                            text = restaurant.name, // Use the name from the Entity
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Food Items for this Restaurant
                items(items) { item ->
                    CartItemRow(
                        item = item,
                        onIncrement = {
                            viewModel.updateQty(
                                item.itemId,
                                item.restoId,
                                item.quantity + 1
                            )
                        },
                        onDecrement = {
                            viewModel.updateQty(
                                item.itemId,
                                item.restoId,
                                item.quantity - 1
                            )
                        }
                    )

                }

                item {
//                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        thickness = 0.5.dp
                    )
                }
            }

            // Bill Summary Section
            item {
                BillSummary(totalBill)
            }

        }

    }
}

@Composable
fun BillSummary(total: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Bill Total:", style = MaterialTheme.typography.titleLarge)
        Text("₹ $total", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun EmptyCartView(
    onNavigateBack: () -> Unit,
    cartEmptyRedirect: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Your Cart is Empty",
            style = MaterialTheme.typography.displayMedium,
        )
        Image(
            painter = painterResource(R.drawable.cart_empty),
            contentDescription = "Your cart is empty",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .height(250.dp)
                .padding(vertical = 5.dp)
        )
        TextButton(
            onClick = { cartEmptyRedirect() }
        ) {
            Text(
                "Explore restaurants and dishes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "₹${item.price / 100}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Stepper Control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Text("-", fontWeight = FontWeight.Bold)
            }
            Text(
                text = "${item.quantity}",
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Text("+", fontWeight = FontWeight.Bold)
            }
        }
    }
}
