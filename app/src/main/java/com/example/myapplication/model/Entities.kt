package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val housekeeperId: String,
    val housekeeperName: String,
    val housekeeperImageUrl: String,
    val status: String,
    val dateTime: String,
    val totalAmount: Double,
    val services: String = "",
    val address: String = "",
    val notes: String = "",
    val rating: Int? = null,
    val tipAmount: Double = 0.0,
    val promoCode: String? = null,
    val discountAmount: Double = 0.0
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val housekeeperId: String
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val label: String,
    val fullAddress: String,
    val apartmentUnit: String = "",
    val instructions: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String,
    val email: String,
    val phone: String = "",
    val profileImageUrl: String = "",
    val referralCode: String = "",
    val loyaltyPoints: Int = 0,
    val preferredLanguage: String = "en",
    val notificationsEnabled: Boolean = true,
    val emailNotifications: Boolean = true,
    val smsNotifications: Boolean = false
)

@Entity(tableName = "promo_codes_used")
data class UsedPromoCodeEntity(
    @PrimaryKey val code: String,
    val usedAt: Long = System.currentTimeMillis()
)
