package com.phoenix.tech.foodapp.ui.state

import com.phoenix.tech.foodapp.data.model.RestoListingItem

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val restoListingItems: List<RestoListingItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}