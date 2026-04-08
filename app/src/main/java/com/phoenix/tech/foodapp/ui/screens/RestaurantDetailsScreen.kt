package com.phoenix.tech.foodapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.data.local.entities.CartItemEntity
import com.phoenix.tech.foodapp.data.local.entities.toCartEntity
import com.phoenix.tech.foodapp.data.local.entities.toEntity
import com.phoenix.tech.foodapp.data.model.RestoDetailsMenuCategory
import com.phoenix.tech.foodapp.data.model.RestoDetailsResult
import com.phoenix.tech.foodapp.data.repository.CartRepository
import com.phoenix.tech.foodapp.data.repository.SessionManager
import com.phoenix.tech.foodapp.ui.components.FoodItemRow
import com.phoenix.tech.foodapp.ui.components.RestaurantInfoHeader
import com.phoenix.tech.foodapp.ui.state.CartViewModelFactory
import com.phoenix.tech.foodapp.ui.state.RestoDetailsUiState
import com.phoenix.tech.foodapp.ui.state.RestoDetailsViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RestaurantDetailsScreen(
    restoId: String, restoName: String, imgId: String,
    avgRating: Double, totalRatingsString: String,
    slaTime: String, slaDist: String,
    cuisines: String,
    cartRepository: CartRepository,
    sessionManager: SessionManager,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    // Create the factory
    val factory = remember(cartRepository, sessionManager) {
        CartViewModelFactory(cartRepository, sessionManager)
    }
    // Pass the factory to the viewModel() function
    val viewModel: RestoDetailsViewModel = viewModel(factory = factory)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Trigger fetch when the screen is first displayed
    LaunchedEffect(restoId) {
        viewModel.fetchRestaurantDetails(restoId)
    }

    Column {
        // Render the header IMMEDIATELY so the transition has a target
        RestaurantInfoHeader(
            restoId = restoId,
            restoName = restoName,
            imgId = imgId,
            avgRating = avgRating,
            totalRatingsString = totalRatingsString,
            slaTime = slaTime,
            slaDist = slaDist,
            cuisines= cuisines,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )

        when (val state = uiState) {
            is RestoDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator()
                }
            }

            is RestoDetailsUiState.Success -> {
                val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

                RestaurantDetailsSuccessResultLayout(
                    restoDetailsResult = state.restoDetailsResult,
                    cartItems = cartItems,
                    onUpdateCart = { item, qty ->
                        viewModel.updateCart(
                            restaurant = state.restoDetailsResult.restoInfo.toEntity(),
                            item = item,
                            newQty = qty
                        )
                    }
                )
            }

            is RestoDetailsUiState.Error -> {
                // TODO: Error UI here
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
                        "An error occurred while fetching restaurant details",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }


}

@Composable
fun RestaurantDetailsSuccessResultLayout(
    restoDetailsResult: RestoDetailsResult,
    cartItems: List<CartItemEntity>,
    onUpdateCart: (item: CartItemEntity, qty: Int) -> Unit
) {
    var selectedCategoryIdx by remember { mutableIntStateOf(0) }
    val currentCategory = restoDetailsResult.categoryItems[selectedCategoryIdx]

    Column(modifier = Modifier.fillMaxSize()) {
        // --- HORIZONTAL CATEGORY CHIPS ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            itemsIndexed(restoDetailsResult.categoryItems) { index, category ->
                FilterChip(
                    selected = selectedCategoryIdx == index,
                    onClick = { selectedCategoryIdx = index },
                    label = { Text(category.title ?: "Menu") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        // --- DYNAMIC CONTENT AREA ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(15.dp)
        ) {
            // Case A: Individual Items are directly in the category
            currentCategory.itemCards?.let { items ->
                items(items) { wrapper ->
                    val apiItem = wrapper.card.info
                    // Check if this item is in the cart
                    val cartEntry = cartItems.find { it.itemId == apiItem.id }
                    val currentQty = cartEntry?.quantity ?: 0

                    FoodItemRow(
                        item = wrapper.card.info,
                        quantity = currentQty,
                        onQuantityChange = { newQty ->
                            // 2. Convert API item to Entity and call the update lambda
                            val entity = cartEntry
                                ?: apiItem.toCartEntity(restoDetailsResult.restoInfo.id)
                            onUpdateCart(entity, newQty)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            // Case B: Subcategories exist
            currentCategory.subCategories?.let { subCategories ->
                items(subCategories) { subCategory ->
                    SubCategoryDropdown(
                        subCategory = subCategory,
                        cartItems = cartItems,
                        restoId = restoDetailsResult.restoInfo.id,
                        onUpdateCart = onUpdateCart
                    )
                }
            }
        }
    }
}

@Composable
fun SubCategoryDropdown(
    subCategory: RestoDetailsMenuCategory,
    cartItems: List<CartItemEntity>,
    restoId: String,
    onUpdateCart: (item: CartItemEntity, qty: Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${subCategory.title} (${subCategory.itemCards?.size ?: 0})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    painter = painterResource(
                        if (isExpanded) R.drawable.ic_up
                        else R.drawable.ic_down
                    ),
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                subCategory.itemCards?.forEach { wrapper ->
                    val apiItem = wrapper.card.info
                    val cartEntry = cartItems.find { it.itemId == apiItem.id }

                    FoodItemRow(
                        item = apiItem,
                        quantity = cartEntry?.quantity ?: 0,
                        onQuantityChange = { newQty ->
                            val entity = cartEntry
                                ?: apiItem.toCartEntity(restoId)
                            onUpdateCart(entity, newQty)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}









