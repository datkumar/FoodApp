package com.phoenix.tech.foodapp.ui.state

import com.phoenix.tech.foodapp.data.model.RestoDetailsResult

sealed class RestoDetailsUiState {
    object Loading : RestoDetailsUiState()

    data class Success(
        val restoDetailsResult: RestoDetailsResult
    ) : RestoDetailsUiState()

    data class Error(val message: String) : RestoDetailsUiState()
}