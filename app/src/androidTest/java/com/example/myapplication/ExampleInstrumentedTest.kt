package com.example.myapplication

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.BookingDao
import com.example.myapplication.data.local.FavoriteDao
import com.example.myapplication.data.local.AddressDao
import com.example.myapplication.model.BookingEntity
import com.example.myapplication.model.FavoriteEntity
import com.example.myapplication.model.AddressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented tests that run on an Android device or emulator.
 * Tests Room database operations: bookings, favorites, and addresses.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTest {

    private lateinit var database: AppDatabase
    private lateinit var bookingDao: BookingDao
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var addressDao: AddressDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bookingDao = database.bookingDao()
        favoriteDao = database.favoriteDao()
        addressDao = database.addressDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    // ── Booking Tests ──

    @Test
    fun insertBooking_andRetrieve_returnsCorrectBooking() = runBlocking {
        val booking = BookingEntity(
            id = "test-booking-1",
            housekeeperId = "hk-1",
            housekeeperName = "Test Housekeeper",
            housekeeperImageUrl = "",
            status = "CONFIRMED",
            dateTime = "2026-03-01 10:00",
            totalAmount = 75.0,
            services = "Cleaning",
            address = "123 Test St"
        )

        bookingDao.insertBooking(booking)
        val result = bookingDao.getBookingById("test-booking-1")

        assertNotNull("Booking should be found", result)
        assertEquals("test-booking-1", result!!.id)
        assertEquals("Test Housekeeper", result.housekeeperName)
        assertEquals(75.0, result.totalAmount, 0.01)
    }

    @Test
    fun updateBookingStatus_changesStatus() = runBlocking {
        val booking = BookingEntity(
            id = "test-booking-2",
            housekeeperId = "hk-1",
            housekeeperName = "Test Housekeeper",
            housekeeperImageUrl = "",
            status = "PENDING",
            dateTime = "2026-03-01 10:00",
            totalAmount = 50.0,
            services = "Cleaning",
            address = "456 Test Ave"
        )

        bookingDao.insertBooking(booking)
        bookingDao.updateStatus("test-booking-2", "CONFIRMED")
        val result = bookingDao.getBookingById("test-booking-2")

        assertEquals("CONFIRMED", result!!.status)
    }

    @Test
    fun updateBookingRating_setsRating() = runBlocking {
        val booking = BookingEntity(
            id = "test-booking-3",
            housekeeperId = "hk-1",
            housekeeperName = "Test Housekeeper",
            housekeeperImageUrl = "",
            status = "COMPLETED",
            dateTime = "2026-03-01 10:00",
            totalAmount = 60.0,
            services = "Cleaning",
            address = "789 Test Blvd"
        )

        bookingDao.insertBooking(booking)
        bookingDao.updateRating("test-booking-3", 5)
        val result = bookingDao.getBookingById("test-booking-3")

        assertEquals(5, result!!.rating)
    }

    @Test
    fun getAllBookings_returnsAllInsertedBookings() = runBlocking {
        val booking1 = BookingEntity(
            id = "b1", housekeeperId = "hk-1", housekeeperName = "HK1",
            housekeeperImageUrl = "", status = "CONFIRMED",
            dateTime = "2026-03-01", totalAmount = 50.0,
            services = "Cleaning", address = "Addr1"
        )
        val booking2 = BookingEntity(
            id = "b2", housekeeperId = "hk-2", housekeeperName = "HK2",
            housekeeperImageUrl = "", status = "PENDING",
            dateTime = "2026-03-02", totalAmount = 75.0,
            services = "Laundry", address = "Addr2"
        )

        bookingDao.insertBooking(booking1)
        bookingDao.insertBooking(booking2)

        val bookings = bookingDao.getAllBookings().first()
        assertEquals(2, bookings.size)
    }

    // ── Favorite Tests ──

    @Test
    fun insertFavorite_andRetrieve_returnsFavorite() = runBlocking {
        val favorite = FavoriteEntity(housekeeperId = "hk-fav-1")
        favoriteDao.insertFavorite(favorite)

        val result = favoriteDao.getFavorite("hk-fav-1")
        assertNotNull("Favorite should be found", result)
        assertEquals("hk-fav-1", result!!.housekeeperId)
    }

    @Test
    fun deleteFavorite_removesFavoriteFromDb() = runBlocking {
        val favorite = FavoriteEntity(housekeeperId = "hk-fav-2")
        favoriteDao.insertFavorite(favorite)
        favoriteDao.deleteFavorite(favorite)

        val result = favoriteDao.getFavorite("hk-fav-2")
        assertNull("Favorite should be deleted", result)
    }

    @Test
    fun getAllFavorites_returnsAllFavorites() = runBlocking {
        favoriteDao.insertFavorite(FavoriteEntity("hk-f1"))
        favoriteDao.insertFavorite(FavoriteEntity("hk-f2"))
        favoriteDao.insertFavorite(FavoriteEntity("hk-f3"))

        val favorites = favoriteDao.getAllFavorites().first()
        assertEquals(3, favorites.size)
    }

    // ── Address Tests ──

    @Test
    fun insertAddress_andRetrieve_returnsAddress() = runBlocking {
        val address = AddressEntity(
            id = "addr-1",
            label = "Home",
            fullAddress = "123 Main St, Test City",
            latitude = 23.588,
            longitude = 58.382,
            isDefault = true
        )
        addressDao.insertAddress(address)

        val addresses = addressDao.getAllAddresses().first()
        assertTrue("Should have at least one address", addresses.isNotEmpty())
        assertEquals("Home", addresses.first().label)
    }

    @Test
    fun setDefaultAddress_onlyOneIsDefault() = runBlocking {
        val addr1 = AddressEntity(
            id = "addr-d1", label = "Home", fullAddress = "Home St",
            latitude = 0.0, longitude = 0.0, isDefault = true
        )
        val addr2 = AddressEntity(
            id = "addr-d2", label = "Work", fullAddress = "Work St",
            latitude = 0.0, longitude = 0.0, isDefault = false
        )

        addressDao.insertAddress(addr1)
        addressDao.insertAddress(addr2)

        // Set addr2 as default
        addressDao.clearDefaultAddresses()
        addressDao.setDefaultAddress("addr-d2")

        val addresses = addressDao.getAllAddresses().first()
        val defaultAddresses = addresses.filter { it.isDefault }

        assertEquals("Only one address should be default", 1, defaultAddresses.size)
        assertEquals("addr-d2", defaultAddresses.first().id)
    }

    @Test
    fun deleteAddress_removesAddress() = runBlocking {
        val address = AddressEntity(
            id = "addr-del", label = "Delete Me", fullAddress = "Delete St",
            latitude = 0.0, longitude = 0.0, isDefault = false
        )
        addressDao.insertAddress(address)
        addressDao.deleteAddress(address)

        val addresses = addressDao.getAllAddresses().first()
        assertTrue("Deleted address should not be in list",
            addresses.none { it.id == "addr-del" })
    }
}