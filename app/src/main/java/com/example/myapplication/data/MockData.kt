package com.example.myapplication.data

import com.example.myapplication.model.*
import java.util.Calendar

object MockData {
    val categories = listOf("All", "Standard", "Deep Clean", "Laundry", "Move-in/out", "Pet Friendly", "Eco-Friendly", "Premium")
    
    val housekeepers = listOf(
        Housekeeper(
            id = "1",
            name = "Alice Johnson",
            rating = 4.8f,
            reviewCount = 124,
            pricePerHour = 25.0,
            description = "Passionate about creating clean and healthy living environments. Specializing in deep cleaning and meticulous organization. I use specialized equipment to ensure a dust-free home.",
            experienceYears = 5,
            services = listOf("Deep Clean", "Standard", "Organizing"),
            imageUrl = "https://images.unsplash.com/photo-1554151228-14d9def656e4?q=80&w=1972&auto=format&fit=crop",
            location = "Downtown, LA",
            availability = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            completedJobs = 342,
            responseTime = "< 30 min",
            languages = listOf("English", "Spanish"),
            badges = listOf("Top Rated", "Quick Responder")
        ),
        Housekeeper(
            id = "2",
            name = "Bob Smith",
            rating = 4.5f,
            reviewCount = 89,
            pricePerHour = 20.0,
            description = "Reliable and efficient cleaning professional. I take pride in my work and ensure every corner sparkles. Great with pets and larger homes.",
            experienceYears = 3,
            services = listOf("Standard", "Laundry", "Windows", "Pet Friendly"),
            imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1974&auto=format&fit=crop",
            location = "Santa Monica, CA",
            availability = listOf("Sat", "Sun"),
            completedJobs = 156,
            responseTime = "< 1 hour",
            languages = listOf("English"),
            badges = listOf("Pet Expert")
        ),
        Housekeeper(
            id = "3",
            name = "Carol White",
            rating = 4.9f,
            reviewCount = 210,
            pricePerHour = 32.0,
            description = "Eco-friendly cleaning expert. Using non-toxic products to keep your home safe for family and pets. Certified in sustainable cleaning practices.",
            experienceYears = 8,
            services = listOf("Eco-Friendly", "Deep Clean", "Move-in/out"),
            imageUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1974&auto=format&fit=crop",
            location = "Beverly Hills, CA",
            availability = listOf("Mon", "Wed", "Fri"),
            completedJobs = 567,
            responseTime = "< 15 min",
            languages = listOf("English", "French"),
            badges = listOf("Eco Champion", "Top Rated", "Superhost")
        ),
        Housekeeper(
            id = "4",
            name = "David Brown",
            rating = 4.2f,
            reviewCount = 45,
            pricePerHour = 18.0,
            description = "Quick and affordable cleaning for those with busy schedules. Available for last-minute bookings and studio apartments.",
            experienceYears = 2,
            services = listOf("Standard", "Kitchen", "Bathroom"),
            imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=1974&auto=format&fit=crop",
            location = "Hollywood, CA",
            availability = listOf("Tue", "Thu", "Sat"),
            completedJobs = 89,
            responseTime = "< 2 hours",
            languages = listOf("English", "Korean"),
            badges = listOf("Budget Friendly")
        ),
        Housekeeper(
            id = "5",
            name = "Eve Davis",
            rating = 5.0f,
            reviewCount = 312,
            pricePerHour = 45.0,
            description = "Premium home management and cleaning. High attention to detail for luxury residences. Specialized in delicate surfaces and fine furniture care.",
            experienceYears = 12,
            services = listOf("Premium", "Deep Clean", "Laundry"),
            imageUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=2070&auto=format&fit=crop",
            location = "Malibu, CA",
            availability = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
            completedJobs = 892,
            responseTime = "< 10 min",
            languages = listOf("English", "Italian", "Portuguese"),
            badges = listOf("Premium Pro", "Top Rated", "5-Star Expert")
        ),
        Housekeeper(
            id = "6",
            name = "Frank Garcia",
            rating = 4.7f,
            reviewCount = 178,
            pricePerHour = 28.0,
            description = "Move-in/out specialist with over 6 years of experience. I ensure your old place gets the deposit back and your new place feels like home.",
            experienceYears = 6,
            services = listOf("Move-in/out", "Deep Clean", "Standard"),
            imageUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?q=80&w=2070&auto=format&fit=crop",
            location = "Pasadena, CA",
            availability = listOf("Mon", "Wed", "Fri", "Sat"),
            completedJobs = 423,
            responseTime = "< 45 min",
            languages = listOf("English", "Spanish"),
            badges = listOf("Move Expert", "Deposit Guarantee")
        )
    )

    val nextSevenDays = (0..6).map { dayOffset ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
        calendar.time
    }

    val timeSlots = listOf(
        TimeSlot("08:00 AM", true),
        TimeSlot("09:00 AM", true),
        TimeSlot("10:00 AM", true),
        TimeSlot("11:00 AM", false),
        TimeSlot("12:00 PM", true),
        TimeSlot("01:00 PM", true),
        TimeSlot("02:00 PM", true),
        TimeSlot("03:00 PM", true),
        TimeSlot("04:00 PM", false),
        TimeSlot("05:00 PM", true),
        TimeSlot("06:00 PM", true)
    )

    val reviews = listOf(
        Review("r1", "1", "John Doe", "https://i.pravatar.cc/150?u=john", 5, "Alice did an amazing job! My house has never been cleaner. She paid attention to every detail and even organized my closet. Highly recommend!", "2 days ago", helpfulCount = 12),
        Review("r2", "1", "Sarah Smith", "https://i.pravatar.cc/150?u=sarah", 4, "Very professional and punctual. Arrived on time and completed everything efficiently. Will book again.", "1 week ago", helpfulCount = 8),
        Review("r3", "3", "Mike Ross", "https://i.pravatar.cc/150?u=mike", 5, "Carol's eco-friendly products smell wonderful and work great. My home feels fresh without any harsh chemical odors.", "3 days ago", helpfulCount = 15),
        Review("r4", "2", "Emily Chen", "https://i.pravatar.cc/150?u=emily", 5, "Bob is fantastic with pets! My dog usually gets anxious around strangers, but Bob knew exactly how to handle the situation.", "5 days ago", helpfulCount = 6),
        Review("r5", "5", "Jessica Williams", "https://i.pravatar.cc/150?u=jessica", 5, "Eve provides the most premium cleaning service I've ever experienced. Worth every penny for the level of detail she provides.", "1 day ago", helpfulCount = 22)
    )

    val myBookings = listOf(
        Booking("b1", "1", "Alice Johnson", "https://images.unsplash.com/photo-1554151228-14d9def656e4?q=80&w=1972&auto=format&fit=crop", BookingStatus.COMPLETED, "Oct 12, 10:00 AM", 75.0, rating = 5),
        Booking("b2", "3", "Carol White", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1974&auto=format&fit=crop", BookingStatus.ON_WAY, "Today, 02:00 PM", 96.0, trackingProgress = 0.6f)
    )

    val chats = listOf(
        Chat("c1", "Alice Johnson", "https://images.unsplash.com/photo-1554151228-14d9def656e4?q=80&w=1972&auto=format&fit=crop", "See you at 10 AM tomorrow!", "09:45 AM", 1, isOnline = true),
        Chat("c2", "Bob Smith", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=1974&auto=format&fit=crop", "I've arrived at the location.", "Yesterday", 0, isOnline = false),
        Chat("c3", "Carol White", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=1974&auto=format&fit=crop", "Thank you for the wonderful review! 😊", "2 days ago", 0, isOnline = true)
    )

    val messagesByChatId = mapOf(
        "c1" to listOf(
            Message("m1", "c1", "Hi Alice, are you available tomorrow?", "09:00 AM", true, isRead = true),
            Message("m2", "c1", "Yes, I am! What time works for you?", "09:15 AM", false, isRead = true),
            Message("m3", "c1", "10 AM would be perfect.", "09:30 AM", true, isRead = true),
            Message("m4", "c1", "See you at 10 AM tomorrow! 😊", "09:45 AM", false, isRead = true)
        ),
        "c2" to listOf(
            Message("m5", "c2", "Hi Bob, I wanted to check the booking details.", "Yesterday 10:00 AM", true, isRead = true),
            Message("m6", "c2", "Sure! I'll be doing standard cleaning for 2 hours.", "Yesterday 10:05 AM", false, isRead = true),
            Message("m7", "c2", "I've arrived at the location.", "Yesterday 10:30 AM", false, isRead = true)
        ),
        "c3" to listOf(
            Message("m8", "c3", "Carol, the cleaning was absolutely amazing!", "2 days ago 03:00 PM", true, isRead = true),
            Message("m9", "c3", "Thank you so much! It was a pleasure working with you.", "2 days ago 03:15 PM", false, isRead = true),
            Message("m10", "c3", "Thank you for the wonderful review! 😊", "2 days ago 03:20 PM", false, isRead = true)
        )
    )

    // Convenience accessor — falls back to empty list for unknown chat IDs
    val messages = messagesByChatId["c1"] ?: emptyList()

    val notifications = listOf(
        AppNotification("n1", "Booking Confirmed", "Your booking with Alice Johnson has been confirmed for tomorrow at 10:00 AM.", NotificationType.BOOKING_CONFIRMED, "2 hours ago"),
        AppNotification("n2", "New Message", "Bob Smith: I've arrived at the location.", NotificationType.MESSAGE, "Yesterday"),
        AppNotification("n3", "Special Offer", "Get 20% off your next deep cleaning with code CLEAN20.", NotificationType.PROMOTION, "3 days ago"),
        AppNotification("n4", "Rate Your Experience", "How was your cleaning with Carol White? Leave a review!", NotificationType.REVIEW_REQUEST, "1 day ago"),
        AppNotification("n5", "Reminder", "Your booking with Eve Davis is tomorrow at 9:00 AM", NotificationType.REMINDER, "Just now")
    )

    val servicePackages = listOf(
        ServicePackage(
            id = "pkg1",
            name = "Basic Clean",
            description = "Perfect for weekly maintenance",
            services = listOf("Dusting", "Vacuuming", "Mopping", "Bathroom Clean"),
            durationHours = 2,
            price = 50.0,
            originalPrice = 60.0
        ),
        ServicePackage(
            id = "pkg2",
            name = "Deep Clean",
            description = "Thorough cleaning for a fresh start",
            services = listOf("All Basic Services", "Inside Cabinets", "Appliance Cleaning", "Window Cleaning"),
            durationHours = 4,
            price = 120.0,
            originalPrice = 150.0,
            isPopular = true
        ),
        ServicePackage(
            id = "pkg3",
            name = "Move-in/out",
            description = "Complete cleaning for moving",
            services = listOf("All Deep Clean Services", "Oven Cleaning", "Fridge Cleaning", "Wall Spot Cleaning"),
            durationHours = 6,
            price = 200.0,
            originalPrice = 250.0
        )
    )

    val promoCodes = listOf(
        PromoCode("CLEAN20", "20% off your first booking", discountPercent = 20, minOrderAmount = 50.0, maxDiscount = 30.0),
        PromoCode("WELCOME10", "Welcome discount - $10 off", discountAmount = 10.0, minOrderAmount = 40.0),
        PromoCode("DEEP30", "30% off deep cleaning", discountPercent = 30, minOrderAmount = 100.0, maxDiscount = 50.0),
        PromoCode("REFER25", "Referral bonus - 25% off", discountPercent = 25, minOrderAmount = 60.0, maxDiscount = 40.0)
    )

    val loyaltyRewards = listOf(
        LoyaltyReward("lr1", "Free Standard Clean", "Redeem for a free 2-hour standard cleaning", 500),
        LoyaltyReward("lr2", "$20 Credit", "Add $20 to your wallet balance", 200),
        LoyaltyReward("lr3", "Premium Upgrade", "Free upgrade to premium service", 300),
        LoyaltyReward("lr4", "Priority Booking", "Skip the queue for 3 months", 400)
    )
}
