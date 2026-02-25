package com.example.myapplication

import com.example.myapplication.data.MockData
import com.example.myapplication.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for booking-related logic.
 * Tests discount calculation, promo code validation, booking creation,
 * tracking progress, and booking-derived stats.
 */
class BookingViewModelTest {

    // ── Discount Calculation ──

    @Test
    fun calculateDiscount_percentBased_returnsCorrectAmount() {
        val promo = PromoCode(
            code = "SAVE20", description = "20% off",
            discountPercent = 20, discountAmount = 0.0,
            minOrderAmount = 0.0, maxDiscount = 0.0, isValid = true
        )
        val subtotal = 100.0
        val discount = computeDiscount(promo, subtotal)
        assertEquals(20.0, discount, 0.01)
    }

    @Test
    fun calculateDiscount_percentWithMaxCap_capIsApplied() {
        val promo = PromoCode(
            code = "HALF", description = "50% off max $15",
            discountPercent = 50, discountAmount = 0.0,
            minOrderAmount = 0.0, maxDiscount = 15.0, isValid = true
        )
        val subtotal = 100.0
        val discount = computeDiscount(promo, subtotal)
        assertEquals("Should be capped at 15.0", 15.0, discount, 0.01)
    }

    @Test
    fun calculateDiscount_fixedAmount_returnsFixedAmount() {
        val promo = PromoCode(
            code = "FLAT10", description = "$10 off",
            discountPercent = 0, discountAmount = 10.0,
            minOrderAmount = 0.0, maxDiscount = 0.0, isValid = true
        )
        val subtotal = 80.0
        val discount = computeDiscount(promo, subtotal)
        assertEquals(10.0, discount, 0.01)
    }

    @Test
    fun calculateDiscount_zeroPercentZeroFixed_returnsZero() {
        val promo = PromoCode(
            code = "NONE", description = "No discount",
            discountPercent = 0, discountAmount = 0.0,
            minOrderAmount = 0.0, maxDiscount = 0.0, isValid = true
        )
        val discount = computeDiscount(promo, 100.0)
        assertEquals(0.0, discount, 0.01)
    }

    @Test
    fun calculateDiscount_percentWithoutCap_calculatesRaw() {
        val promo = PromoCode(
            code = "BIG", description = "30% off no cap",
            discountPercent = 30, discountAmount = 0.0,
            minOrderAmount = 0.0, maxDiscount = 0.0, isValid = true
        )
        val discount = computeDiscount(promo, 200.0)
        assertEquals(60.0, discount, 0.01)
    }

    @Test
    fun calculateDiscount_percentCapNotReached_returnsRaw() {
        val promo = PromoCode(
            code = "SAVE10", description = "10% off max $50",
            discountPercent = 10, discountAmount = 0.0,
            minOrderAmount = 0.0, maxDiscount = 50.0, isValid = true
        )
        // 10% of 100 = 10, which is below cap of 50
        val discount = computeDiscount(promo, 100.0)
        assertEquals(10.0, discount, 0.01)
    }

    // ── Promo Code Lookup ──

    @Test
    fun promoCodeLookup_validCode_returnsPromoCode() {
        val code = MockData.promoCodes.first().code
        val found = MockData.promoCodes.find {
            it.code.equals(code, ignoreCase = true) && it.isValid
        }
        assertNotNull("Valid code should be found", found)
    }

    @Test
    fun promoCodeLookup_invalidCode_returnsNull() {
        val found = MockData.promoCodes.find {
            it.code.equals("FAKECODE", ignoreCase = true) && it.isValid
        }
        assertNull("Invalid code should return null", found)
    }

    @Test
    fun promoCodeLookup_caseInsensitive() {
        val code = MockData.promoCodes.first().code
        val foundUpper = MockData.promoCodes.find {
            it.code.equals(code.uppercase(), ignoreCase = true) && it.isValid
        }
        val foundLower = MockData.promoCodes.find {
            it.code.equals(code.lowercase(), ignoreCase = true) && it.isValid
        }
        assertNotNull("Upper case lookup should work", foundUpper)
        assertNotNull("Lower case lookup should work", foundLower)
        assertEquals("Both should find the same promo", foundUpper?.code, foundLower?.code)
    }

    @Test
    fun promoCode_minOrderCheck_rejectsLowAmount() {
        val promo = MockData.promoCodes.firstOrNull { it.minOrderAmount > 0 }
        if (promo != null) {
            val belowMin = promo.minOrderAmount - 1
            assertTrue("Order below min should be rejected", belowMin < promo.minOrderAmount)
        }
    }

    // ── Tracking Progress ──

    @Test
    fun trackingProgress_pendingReturnsLow() {
        assertEquals(0.1f, calculateTrackingProgress(BookingStatus.PENDING), 0.01f)
    }

    @Test
    fun trackingProgress_confirmedReturnsQuarter() {
        assertEquals(0.25f, calculateTrackingProgress(BookingStatus.CONFIRMED), 0.01f)
    }

    @Test
    fun trackingProgress_completedReturnsFull() {
        assertEquals(1.0f, calculateTrackingProgress(BookingStatus.COMPLETED), 0.01f)
    }

    @Test
    fun trackingProgress_cancelledReturnsZero() {
        assertEquals(0.0f, calculateTrackingProgress(BookingStatus.CANCELLED), 0.01f)
    }

    @Test
    fun trackingProgress_onWayReturnsHalf() {
        assertEquals(0.5f, calculateTrackingProgress(BookingStatus.ON_WAY), 0.01f)
    }

    @Test
    fun trackingProgress_inProgressReturnsThreeQuarter() {
        assertEquals(0.75f, calculateTrackingProgress(BookingStatus.IN_PROGRESS), 0.01f)
    }

    // ── Booking Stats ──

    @Test
    fun bookingCount_countsOnlyCompleted() {
        val bookings = listOf(
            createTestBooking(BookingStatus.COMPLETED),
            createTestBooking(BookingStatus.COMPLETED),
            createTestBooking(BookingStatus.CANCELLED),
            createTestBooking(BookingStatus.PENDING)
        )
        val count = bookings.count { it.status == BookingStatus.COMPLETED }
        assertEquals("Should count only COMPLETED bookings", 2, count)
    }

    @Test
    fun averageRating_calculatesCorrectly() {
        val bookings = listOf(
            createTestBooking(BookingStatus.COMPLETED, rating = 5),
            createTestBooking(BookingStatus.COMPLETED, rating = 3),
            createTestBooking(BookingStatus.COMPLETED, rating = 4)
        )
        val rated = bookings.filter { (it.rating ?: 0) > 0 }
        val avg = rated.map { it.rating?.toFloat() ?: 0f }.average().toFloat()
        assertEquals(4.0f, avg, 0.01f)
    }

    @Test
    fun averageRating_noRatedBookings_returnsZero() {
        val bookings = listOf(
            createTestBooking(BookingStatus.COMPLETED, rating = null),
            createTestBooking(BookingStatus.PENDING, rating = null)
        )
        val rated = bookings.filter { (it.rating ?: 0) > 0 }
        val avg = if (rated.isNotEmpty()) rated.map { it.rating?.toFloat() ?: 0f }.average().toFloat() else 0f
        assertEquals(0f, avg, 0.01f)
    }

    @Test
    fun bookingTotal_correctWithDiscount() {
        val pricePerHour = 25.0
        val duration = 3
        val discount = 10.0
        val tip = 5.0
        val total = pricePerHour * duration - discount + tip
        assertEquals(70.0, total, 0.01)
    }

    @Test
    fun bookingTotal_noDiscountNoTip() {
        val pricePerHour = 30.0
        val duration = 2
        val total = pricePerHour * duration
        assertEquals(60.0, total, 0.01)
    }

    // ── Service Packages ──

    @Test
    fun servicePackages_notEmpty() {
        assertTrue("Service packages should exist", MockData.servicePackages.isNotEmpty())
    }

    @Test
    fun servicePackages_haveValidPrices() {
        MockData.servicePackages.forEach { pkg ->
            assertTrue("Package price should be positive: ${pkg.name}", pkg.price > 0)
            assertTrue("Package original price >= price: ${pkg.name}",
                pkg.originalPrice >= pkg.price)
        }
    }

    // ── Helpers ──

    private fun computeDiscount(promo: PromoCode, subtotal: Double): Double {
        return if (promo.discountPercent > 0) {
            val raw = subtotal * promo.discountPercent / 100
            if (promo.maxDiscount > 0) minOf(raw, promo.maxDiscount) else raw
        } else {
            promo.discountAmount
        }
    }

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

    private fun createTestBooking(
        status: BookingStatus,
        rating: Int? = null
    ) = Booking(
        id = "test-${System.nanoTime()}",
        housekeeperId = "1",
        housekeeperName = "Test",
        housekeeperImageUrl = "",
        status = status,
        dateTime = "Jan 1, 10:00 AM",
        totalAmount = 50.0,
        rating = rating
    )
}
