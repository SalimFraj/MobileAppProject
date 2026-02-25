package com.example.myapplication.ui.booking

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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for booking lifecycle: creating, cancelling, rating, rebooking,
 * promo codes, and booking-derived stats.
 * Used by BookingScreen, BookingCheckoutSheet, HousekeeperDetailScreen,
 * ProfileScreen, and WalletScreen.
 */
class BookingViewModel(
    application: Application,
    private val repository: HousekeeperRepository
) : AndroidViewModel(application) {

    // Persistent Bookings from Room via Repository
    val myBookings: StateFlow<List<Booking>> = repository.getAllBookingsRaw()
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

    // Dynamic profile stats from bookings
    val bookingCount: StateFlow<Int> = myBookings
        .map { bookings -> bookings.count { it.status == BookingStatus.COMPLETED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageRating: StateFlow<Float> = myBookings
        .map { bookings ->
            val rated = bookings.filter { (it.rating ?: 0) > 0 }
            if (rated.isNotEmpty()) rated.map { it.rating?.toFloat() ?: 0f }.average().toFloat()
            else 0f
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Service Packages
    val servicePackages: List<ServicePackage> = repository.getServicePackages()

    // Promo Code Validation State
    private val _appliedPromoCode = MutableStateFlow<PromoCode?>(null)
    val appliedPromoCode: StateFlow<PromoCode?> = _appliedPromoCode.asStateFlow()

    private val _promoCodeError = MutableStateFlow<String?>(null)
    val promoCodeError: StateFlow<String?> = _promoCodeError.asStateFlow()

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

    fun validatePromoCode(code: String, orderAmount: Double) {
        viewModelScope.launch {
            _promoCodeError.value = null

            if (code.isBlank()) {
                _appliedPromoCode.value = null
                return@launch
            }

            val usedCode = repository.getUsedPromoCode(code.uppercase())
            if (usedCode != null) {
                _promoCodeError.value = "This promo code has already been used"
                return@launch
            }

            val promoCode = repository.findPromoCode(code)

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
            repository.insertBooking(newBooking)

            promoCode?.let {
                repository.markPromoCodeUsed(it.code)
            }

            repository.addLoyaltyPoints(25)
            clearPromoCode()
        }
    }

    fun rebookFromHistory(booking: Booking) {
        viewModelScope.launch {
            val housekeeper = repository.getHousekeeperById(booking.housekeeperId)
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
                repository.insertBooking(newBooking)
            }
        }
    }

    fun rateBooking(bookingId: String, rating: Int) {
        viewModelScope.launch {
            repository.updateBookingRating(bookingId, rating)
            repository.addLoyaltyPoints(10)
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HouseKeepApplication)
                val repository = application.container.housekeeperRepository
                BookingViewModel(application = application, repository = repository)
            }
        }
    }
}
