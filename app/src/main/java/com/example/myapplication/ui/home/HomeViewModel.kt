package com.example.myapplication.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.HouseKeepApplication
import com.example.myapplication.data.HousekeeperRepository
import com.example.myapplication.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the home screen housekeepers list.
 */
data class HousekeepUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val housekeepers: List<Housekeeper> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel for browsing, searching, filtering, and favoriting housekeepers.
 * Used by HousekeeperListScreen, HousekeeperDetailScreen, and FavoritesScreen.
 */
class HomeViewModel(
    application: Application,
    private val repository: HousekeeperRepository
) : AndroidViewModel(application) {

    // --- UiState ---
    private val _uiState = MutableStateFlow(HousekeepUiState())
    val uiState: StateFlow<HousekeepUiState> = _uiState.asStateFlow()

    // Search and Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filter States
    private val _priceRange = MutableStateFlow(15f..150f)
    private val _minRating = MutableStateFlow(0f)
    private val _filterServices = MutableStateFlow<List<String>>(emptyList())

    // Persistent Favorites from Room via Repository
    val favorites: StateFlow<Set<String>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val filteredHousekeepers = combine(
        _searchQuery, _selectedCategory, _priceRange, _minRating, _filterServices
    ) { query, category, price, rating, services ->
        _isLoading.value = true
        _uiState.update { it.copy(isLoading = true) }
        delay(300)
        val result = repository.getHousekeepersSync().filter { h ->
            val matchesQuery = h.name.contains(query, ignoreCase = true) ||
                    h.services.any { it.contains(query, ignoreCase = true) }
            val matchesCategory = category == "All" || h.services.contains(category)
            val matchesPrice = h.pricePerHour >= price.start && h.pricePerHour <= price.endInclusive
            val matchesRating = h.rating >= rating
            val matchesServices = services.isEmpty() || services.any { h.services.contains(it) }

            matchesQuery && matchesCategory && matchesPrice && matchesRating && matchesServices
        }
        _isLoading.value = false
        _uiState.update { it.copy(isLoading = false, housekeepers = result, searchQuery = query, selectedCategory = category) }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getHousekeepersSync())

    // Featured housekeepers for carousel
    val featuredHousekeepers: List<Housekeeper> = repository.getFeaturedHousekeepers()

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryChange(newCategory: String) {
        _selectedCategory.value = newCategory
    }

    fun applyFilters(priceRange: ClosedFloatingPointRange<Float>, rating: Float, services: List<String>) {
        _priceRange.value = priceRange
        _minRating.value = rating
        _filterServices.value = services
    }

    fun toggleFavorite(housekeeperId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(housekeeperId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HouseKeepApplication)
                val repository = application.container.housekeeperRepository
                HomeViewModel(application = application, repository = repository)
            }
        }
    }
}
