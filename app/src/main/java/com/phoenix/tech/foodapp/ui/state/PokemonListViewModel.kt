package com.phoenix.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.phoenix.tech.foodapp.data.model.PokemonListItem
import com.phoenix.tech.foodapp.data.repository.PokemonListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class PokemonListUiState {
    object Loading : PokemonListUiState()

    data class Success(
        val pokemonList: List<PokemonListItem>
    ) : PokemonListUiState()

    data class Error(val message: String) : PokemonListUiState()
}

class PokemonListViewModelFactory(
    private val repository: PokemonListRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokemonListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class PokemonListViewModel(
    private val repository: PokemonListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<PokemonListItem>>(emptyList())
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _isPaginating = MutableStateFlow(false)
    val isPaginating = _isPaginating.asStateFlow()

    init {
        loadPokemon()
    }

    fun loadPokemon(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _isPaginating.value = true
            }

            val newList = repository.getPokemonList(
                forceRefresh = isRefresh
            )
            _uiState.value = newList

            _isRefreshing.value = false
            _isPaginating.value = false
        }
    }
}