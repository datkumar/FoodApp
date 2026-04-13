package com.phoenix.tech.foodapp.ui.state

import com.phoenix.tech.foodapp.data.model.RestoListingItem

sealed class HomeUiState {
    data class Success(
        val restoListingItems: List<RestoListingItem>,
        val isLoadingMore: Boolean = false,  // spinner at bottom of list
        val hasMore: Boolean = true
    ) : HomeUiState()

    object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}