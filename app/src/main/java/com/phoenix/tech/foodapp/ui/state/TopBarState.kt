package com.phoenix.tech.foodapp.ui.state

data class TopBarState(
    val title: String = "",
    val showCart: Boolean = true,
    val showBack: Boolean = false,
    val isHome: Boolean = false,
    val cartItemCount: Int = 0
)
