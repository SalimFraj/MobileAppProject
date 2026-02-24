package com.example.myapplication.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.HouseKeepApplication
import com.example.myapplication.data.MockData
import com.example.myapplication.data.HousekeeperRepository
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Represents the UI state for the home screen housekeepers list.
 * Consolidates loading, data, search, and error states into one object.
 */
data class HousekeepUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val housekeepers: List<Housekeeper> = emptyList(),
    val errorMessage: String? = null
)

class MainViewModel(
    application: Application,
    private val repository: HousekeeperRepository
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val bookingDao = database.bookingDao()
    private val favoriteDao = database.favoriteDao()
    private val addressDao = database.addressDao()
    private val userDao = database.userDao()
    private val promoCodeDao = database.promoCodeDao()

    // --- UiState ---
    private val _uiState = MutableStateFlow(HousekeepUiState())
    val uiState: StateFlow<HousekeepUiState> = _uiState.asStateFlow()

    // Search and Filter State (kept for backward compat with screens)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filter States
    private val _priceRange = MutableStateFlow(15f..150f)
    private val _minRating = MutableStateFlow(0f)
    private val _filterServices = MutableStateFlow<List<String>>(emptyList())

    // User Profile
    val userProfile: StateFlow<UserProfileEntity?> = userDao.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Persistent Bookings from Room with enhanced mapping
    val myBookings: StateFlow<List<Booking>> = bookingDao.getAllBookings()
        .map { entities ->
            entities.map { entity ->
                Booking(
                    id = entity.id,
                    housekeeperId = entity.housekeeperId,
                    housekeeperName = entity.housekeeperName,
                    housekeeperImageUrl = entity.housekeeperImageUrl,
                    status = BookingStatus.valueOf(entity.status),
                    dateTime = entity.dateTime,
                    totalAmount = entity.totalAmount,
                    services = entity.services,
                    address = entity.address,
                    notes = entity.notes,
                    rating = entity.rating,
                    tipAmount = entity.tipAmount,
                    promoCode = entity.promoCode,
                    discountAmount = entity.discountAmount,
                    trackingProgress = calculateTrackingProgress(BookingStatus.valueOf(entity.status))
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Persistent Favorites from Room
    val favorites: StateFlow<Set<String>> = favoriteDao.getAllFavorites()
        .map { entities -> entities.map { it.housekeeperId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Saved Addresses
    val addresses: StateFlow<List<AddressEntity>> = addressDao.getAllAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Service Packages
    val servicePackages: List<ServicePackage> = MockData.servicePackages

    // Promo Code Validation State
    private val _appliedPromoCode = MutableStateFlow<PromoCode?>(null)
    val appliedPromoCode: StateFlow<PromoCode?> = _appliedPromoCode.asStateFlow()

    private val _promoCodeError = MutableStateFlow<String?>(null)
    val promoCodeError: StateFlow<String?> = _promoCodeError.asStateFlow()

    val filteredHousekeepers = combine(
        _searchQuery, _selectedCategory, _priceRange, _minRating, _filterServices
    ) { query, category, price, rating, services ->
        _isLoading.value = true
        _uiState.update { it.copy(isLoading = true) }
        delay(300)
        val result = MockData.housekeepers.filter { h ->
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MockData.housekeepers)

    // Featured housekeepers for carousel
    val featuredHousekeepers: List<Housekeeper> = MockData.housekeepers
        .filter { it.rating >= 4.8f }
        .sortedByDescending { it.reviewCount }

    private fun calculateTrackingProgress(status: BookingStatus): Float {
        return when (status) {
            BookingStatus.PENDING -> 0.1f
            BookingStatus.CONFIRMED -> 0.25f
            BookingStatus.ON_WAY -> 0.5f
            BookingStatus.IN_PROGRESS -> 0.75f
            BookingStatus.COMPLETED -> 1f
            BookingStatus.CANCELLED -> 0f
        }
    }

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

    fun toggleDarkMode() {
        _isDarkMode.value = when(_isDarkMode.value) {
            true -> false
            false -> true
            else -> true
        }
    }

    fun toggleFavorite(housekeeperId: String) {
        viewModelScope.launch {
            if (favorites.value.contains(housekeeperId)) {
                favoriteDao.deleteFavorite(FavoriteEntity(housekeeperId))
            } else {
                favoriteDao.insertFavorite(FavoriteEntity(housekeeperId))
            }
        }
    }

    fun validatePromoCode(code: String, orderAmount: Double) {
        viewModelScope.launch {
            _promoCodeError.value = null
            
            if (code.isBlank()) {
                _appliedPromoCode.value = null
                return@launch
            }

            // Check if already used
            val usedCode = promoCodeDao.getUsedPromoCode(code.uppercase())
            if (usedCode != null) {
                _promoCodeError.value = "This promo code has already been used"
                return@launch
            }

            val promoCode = MockData.promoCodes.find { 
                it.code.equals(code, ignoreCase = true) && it.isValid 
            }

            if (promoCode == null) {
                _promoCodeError.value = "Invalid promo code"
                _appliedPromoCode.value = null
            } else if (orderAmount < promoCode.minOrderAmount) {
                _promoCodeError.value = "Minimum order amount is $${promoCode.minOrderAmount.toInt()}"
                _appliedPromoCode.value = null
            } else {
                _appliedPromoCode.value = promoCode
            }
        }
    }

    fun clearPromoCode() {
        _appliedPromoCode.value = null
        _promoCodeError.value = null
    }

    fun calculateDiscount(subtotal: Double): Double {
        val promo = _appliedPromoCode.value ?: return 0.0
        return if (promo.discountPercent > 0) {
            val discount = subtotal * promo.discountPercent / 100
            if (promo.maxDiscount > 0) minOf(discount, promo.maxDiscount) else discount
        } else {
            promo.discountAmount
        }
    }

    fun bookHousekeeper(
        housekeeper: Housekeeper, 
        dateAndTime: String, 
        duration: Int,
        services: String = "",
        address: String = "",
        notes: String = "",
        tipAmount: Double = 0.0
    ) {
        viewModelScope.launch {
            val subtotal = housekeeper.pricePerHour * duration
            val discount = calculateDiscount(subtotal)
            val promoCode = _appliedPromoCode.value

            val newBooking = BookingEntity(
                id = UUID.randomUUID().toString(),
                housekeeperId = housekeeper.id,
                housekeeperName = housekeeper.name,
                housekeeperImageUrl = housekeeper.imageUrl,
                status = BookingStatus.CONFIRMED.name,
                dateTime = dateAndTime,
                totalAmount = subtotal - discount + tipAmount,
                services = services,
                address = address,
                notes = notes,
                tipAmount = tipAmount,
                promoCode = promoCode?.code,
                discountAmount = discount
            )
            bookingDao.insertBooking(newBooking)

            // Mark promo code as used
            promoCode?.let {
                promoCodeDao.markPromoCodeUsed(UsedPromoCodeEntity(it.code.uppercase()))
            }

            // Add loyalty points
            userDao.addLoyaltyPoints(25)

            // Clear promo code after booking
            clearPromoCode()
        }
    }

    fun rebookFromHistory(booking: Booking) {
        viewModelScope.launch {
            val housekeeper = MockData.housekeepers.find { it.id == booking.housekeeperId }
            if (housekeeper != null) {
                val newBooking = BookingEntity(
                    id = UUID.randomUUID().toString(),
                    housekeeperId = housekeeper.id,
                    housekeeperName = housekeeper.name,
                    housekeeperImageUrl = housekeeper.imageUrl,
                    status = BookingStatus.PENDING.name,
                    dateTime = "Rescheduled - Select new time",
                    totalAmount = booking.totalAmount,
                    services = booking.services,
                    address = booking.address,
                    notes = booking.notes
                )
                bookingDao.insertBooking(newBooking)
            }
        }
    }

    fun rateBooking(bookingId: String, rating: Int) {
        viewModelScope.launch {
            bookingDao.updateRating(bookingId, rating)
            // Award bonus points for rating
            userDao.addLoyaltyPoints(10)
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            bookingDao.updateStatus(bookingId, BookingStatus.CANCELLED.name)
        }
    }

    // Address Management
    fun addAddress(address: AddressEntity) {
        viewModelScope.launch {
            addressDao.insertAddress(address)
        }
    }

    fun deleteAddress(address: AddressEntity) {
        viewModelScope.launch {
            addressDao.deleteAddress(address)
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            addressDao.clearDefaultAddresses()
            addressDao.setDefaultAddress(addressId)
        }
    }

    // Initialize default user profile if not exists
    init {
        viewModelScope.launch {
            if (userProfile.value == null) {
                userDao.insertOrUpdate(
                    UserProfileEntity(
                        name = "Salim Al-Harthy",
                        email = "salim@example.com",
                        referralCode = "SALIM25",
                        loyaltyPoints = 150
                    )
                )
            }
        }
    }

    /**
     * Factory that uses the AppContainer to provide the repository dependency.
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HouseKeepApplication)
                val repository = application.container.housekeeperRepository
                MainViewModel(application = application, repository = repository)
            }
        }
    }
}
