package com.phoenix.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phoenix.tech.foodapp.data.model.PokemonListItem
import com.phoenix.tech.foodapp.data.repository.PokemonListRepository
import kotlinx.coroutines.flow.Flow


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

    // cachedIn(viewModelScope) is essential to survive configuration changes (rotations)
    val pokemonPagingFlow: Flow<PagingData<PokemonListItem>> =
        repository.getPokemonPager().cachedIn(viewModelScope)
}