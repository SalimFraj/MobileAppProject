package com.example.myapplication.model

data class Housekeeper(
    val id: String,
    val name: String,
    val rating: Float,
    val reviewCount: Int,
    val pricePerHour: Double,
    val description: String,
    val experienceYears: Int,
    val services: List<String>,
    val imageUrl: String,
    val location: String = "Los Angeles, CA",
    val isVerified: Boolean = true,
    val availability: List<String> = listOf("Mon", "Wed", "Fri"),
    val completedJobs: Int = 0,
    val responseTime: String = "< 1 hour",
    val languages: List<String> = listOf("English"),
    val badges: List<String> = emptyList()
)

data class Review(
    val id: String,
    val housekeeperId: String = "",
    val userName: String,
    val userImageUrl: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val images: List<String> = emptyList(),
    val helpfulCount: Int = 0
)

data class Booking(
    val id: String,
    val housekeeperId: String,
    val housekeeperName: String,
    val housekeeperImageUrl: String,
    val status: BookingStatus,
    val dateTime: String,
    val totalAmount: Double,
    val services: String = "",
    val address: String = "",
    val notes: String = "",
    val rating: Int? = null,
    val tipAmount: Double = 0.0,
    val promoCode: String? = null,
    val discountAmount: Double = 0.0,
    val trackingProgress: Float = 0f
)

enum class BookingStatus {
    PENDING, CONFIRMED, ON_WAY, IN_PROGRESS, COMPLETED, CANCELLED
}

data class Message(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isFromMe: Boolean,
    val type: MessageType = MessageType.TEXT,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val reactions: List<String> = emptyList()
)

enum class MessageType {
    TEXT, IMAGE, BOOKING_CARD, SYSTEM
}

data class Chat(
    val id: String,
    val participantName: String,
    val participantImageUrl: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false
)

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val time: String,
    val isRead: Boolean = false,
    val actionUrl: String? = null
)

enum class NotificationType {
    BOOKING_CONFIRMED, PROMOTION, SYSTEM, MESSAGE, REMINDER, REVIEW_REQUEST
}

data class ServicePackage(
    val id: String,
    val name: String,
    val description: String,
    val services: List<String>,
    val durationHours: Int,
    val price: Double,
    val originalPrice: Double,
    val isPopular: Boolean = false
)

data class PromoCode(
    val code: String,
    val description: String,
    val discountPercent: Int = 0,
    val discountAmount: Double = 0.0,
    val minOrderAmount: Double = 0.0,
    val maxDiscount: Double = 0.0,
    val expiresAt: String = "",
    val isValid: Boolean = true
)

data class TimeSlot(
    val time: String,
    val isAvailable: Boolean = true,
    val price: Double? = null
)

data class DayAvailability(
    val date: java.util.Date,
    val slots: List<TimeSlot>,
    val isFullyBooked: Boolean = false
)

data class LoyaltyReward(
    val id: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val imageUrl: String = ""
)
