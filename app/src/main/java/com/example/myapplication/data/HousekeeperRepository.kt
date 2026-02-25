package com.example.myapplication.data

import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

/**
 * Repository for housekeepers data providing a clean abstraction
 * over local and mock data sources.
 */
class HousekeeperRepository(private val database: AppDatabase) {

    private val bookingDao = database.bookingDao()
    private val favoriteDao = database.favoriteDao()
    private val addressDao = database.addressDao()
    private val userDao = database.userDao()
    private val promoCodeDao = database.promoCodeDao()

    // ── Housekeepers ──

    /** Synchronous access to all housekeepers (currently MockData). */
    fun getHousekeepersSync(): List<Housekeeper> = MockData.housekeepers

    /** Featured housekeepers filtered by high rating. */
    fun getFeaturedHousekeepers(): List<Housekeeper> =
        MockData.housekeepers.filter { it.rating >= 4.8f }.sortedByDescending { it.reviewCount }

    /** Find a housekeeper by ID. */
    fun getHousekeeperById(id: String): Housekeeper? =
        MockData.housekeepers.find { it.id == id }

    /** Housekeepers with simulated network delay. */
    fun getHousekeepers(): Flow<Resource<List<Housekeeper>>> = flow {
        emit(Resource.Loading)
        delay(300)
        emit(Resource.Success(MockData.housekeepers))
    }

    fun searchHousekeepers(query: String): Flow<Resource<List<Housekeeper>>> = flow {
        emit(Resource.Loading)
        delay(200)
        val results = MockData.housekeepers.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.services.any { s -> s.contains(query, ignoreCase = true) }
        }
        emit(Resource.Success(results))
    }

    // ── Bookings ──

    /** Raw entity flow for ViewModel-level mapping. */
    fun getAllBookingsRaw(): Flow<List<BookingEntity>> = bookingDao.getAllBookings()

    fun getAllBookings(): Flow<List<Booking>> = bookingDao.getAllBookings()
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
                    rating = entity.rating
                )
            }
        }

    suspend fun insertBooking(booking: BookingEntity) {
        bookingDao.insertBooking(booking)
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
        bookingDao.updateStatus(bookingId, status.name)
    }

    suspend fun updateBookingRating(bookingId: String, rating: Int) {
        bookingDao.updateRating(bookingId, rating)
    }

    // ── Favorites ──

    fun getAllFavorites(): Flow<Set<String>> = favoriteDao.getAllFavorites()
        .map { entities -> entities.map { it.housekeeperId }.toSet() }

    suspend fun toggleFavorite(housekeeperId: String) {
        val exists = favoriteDao.getFavorite(housekeeperId) != null
        if (exists) {
            favoriteDao.deleteFavorite(FavoriteEntity(housekeeperId))
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(housekeeperId))
        }
    }

    // ── Addresses ──

    fun getAllAddresses(): Flow<List<AddressEntity>> = addressDao.getAllAddresses()

    suspend fun insertAddress(address: AddressEntity) {
        addressDao.insertAddress(address)
    }

    suspend fun deleteAddress(address: AddressEntity) {
        addressDao.deleteAddress(address)
    }

    suspend fun setDefaultAddress(addressId: String) {
        addressDao.clearDefaultAddresses()
        addressDao.setDefaultAddress(addressId)
    }

    // ── User Profile ──

    fun getUserProfile(): Flow<UserProfileEntity?> = userDao.getUserProfile()

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        userDao.insertOrUpdate(profile)
    }

    suspend fun addLoyaltyPoints(points: Int) {
        userDao.addLoyaltyPoints(points)
    }

    // ── Promo Codes ──

    fun findPromoCode(code: String): PromoCode? {
        return MockData.promoCodes.find {
            it.code.equals(code, ignoreCase = true) && it.isValid
        }
    }

    suspend fun getUsedPromoCode(code: String): UsedPromoCodeEntity? {
        return promoCodeDao.getUsedPromoCode(code)
    }

    suspend fun markPromoCodeUsed(code: String) {
        promoCodeDao.markPromoCodeUsed(UsedPromoCodeEntity(code.uppercase()))
    }

    // ── Reviews ──

    fun getReviewsForHousekeeper(housekeeperId: String): List<Review> {
        return MockData.reviews.filter { it.housekeeperId == housekeeperId }
    }

    // ── Service Packages ──

    fun getServicePackages(): List<ServicePackage> = MockData.servicePackages
}
