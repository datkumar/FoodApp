package com.example.tech.foodapp.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tech.foodapp.data.model.PokemonDetailsResponse
import com.example.tech.foodapp.data.repository.PokemonDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PokemonDetailsUiState {
    data class Success(
        val pokemonDetails: PokemonDetailsResponse
    ) : PokemonDetailsUiState()

    object Loading : PokemonDetailsUiState()
    data class Error(val message: String) : PokemonDetailsUiState()
}


class PokemonDetailsViewModelFactory(
    private val repository: PokemonDetailsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokemonDetailsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class PokemonDetailsViewModel(
    private val repository: PokemonDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonDetailsUiState>(PokemonDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun getPokemonDetails(id: Int) {
        viewModelScope.launch {
            _uiState.value = PokemonDetailsUiState.Loading
            try {
                val result = repository.getPokemonDetails(id)
                if (result != null) {
                    _uiState.value = PokemonDetailsUiState.Success(result)
                } else {
                    _uiState.value = PokemonDetailsUiState.Error("Pokemon not found")
                }
            } catch (e: Exception) {
                _uiState.value = PokemonDetailsUiState.Error(
                    e.message ?: "An Unknown Error occurred"
                )
            }
        }
    }

}