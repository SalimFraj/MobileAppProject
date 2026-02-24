package com.example.myapplication.data.local

import android.content.Context
import androidx.room.*
import com.example.myapplication.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY id DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: String): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateStatus(bookingId: String, status: String)

    @Query("UPDATE bookings SET rating = :rating WHERE id = :bookingId")
    suspend fun updateRating(bookingId: String, rating: Int)

    @Query("SELECT * FROM bookings WHERE status = :status")
    fun getBookingsByStatus(status: String): Flow<List<BookingEntity>>

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE housekeeperId = :housekeeperId")
    suspend fun getFavorite(housekeeperId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses ORDER BY isDefault DESC")
    fun getAllAddresses(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultAddress(): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefaultAddresses()

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :addressId")
    suspend fun setDefaultAddress(addressId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 'current_user'")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET loyaltyPoints = loyaltyPoints + :points WHERE id = 'current_user'")
    suspend fun addLoyaltyPoints(points: Int)
}

@Dao
interface PromoCodeDao {
    @Query("SELECT * FROM promo_codes_used WHERE code = :code")
    suspend fun getUsedPromoCode(code: String): UsedPromoCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markPromoCodeUsed(promoCode: UsedPromoCodeEntity)
}

@Database(
    entities = [
        BookingEntity::class, 
        FavoriteEntity::class, 
        AddressEntity::class,
        UserProfileEntity::class,
        UsedPromoCodeEntity::class
    ], 
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun addressDao(): AddressDao
    abstract fun userDao(): UserDao
    abstract fun promoCodeDao(): PromoCodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "housekeep_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
