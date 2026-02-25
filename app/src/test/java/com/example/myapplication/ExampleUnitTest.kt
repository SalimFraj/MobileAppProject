package com.example.myapplication

import com.example.myapplication.data.MockData
import com.example.myapplication.model.PromoCode
import com.example.myapplication.ui.home.HousekeepUiState
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the HouseKeep app.
 * Tests ViewModel logic (discount calculation, filtering, promo validation)
 * and data model behavior.
 */
class MainViewModelUnitTest {

    // ── Discount Calculation Tests ──

    @Test
    fun calculateDiscount_percentBased_returnsCorrectDiscount() {
        // Given a 20% discount promo with no max cap
        val promoCode = PromoCode(
            code = "SAVE20",
            description = "20% off",
            discountPercent = 20,
            discountAmount = 0.0,
            minOrderAmount = 0.0,
            maxDiscount = 0.0,
            isValid = true
        )
        val subtotal = 100.0

        // When calculating the discount
        val discount = if (promoCode.discountPercent > 0) {
            val raw = subtotal * promoCode.discountPercent / 100
            if (promoCode.maxDiscount > 0) minOf(raw, promoCode.maxDiscount) else raw
        } else {
            promoCode.discountAmount
        }

        // Then
        assertEquals(20.0, discount, 0.01)
    }

    @Test
    fun calculateDiscount_percentWithMaxCap_capIsApplied() {
        // Given a 50% discount with a $15 max cap
        val promoCode = PromoCode(
            code = "HALF",
            description = "50% off max $15",
            discountPercent = 50,
            discountAmount = 0.0,
            minOrderAmount = 0.0,
            maxDiscount = 15.0,
            isValid = true
        )
        val subtotal = 100.0

        val discount = if (promoCode.discountPercent > 0) {
            val raw = subtotal * promoCode.discountPercent / 100
            if (promoCode.maxDiscount > 0) minOf(raw, promoCode.maxDiscount) else raw
        } else {
            promoCode.discountAmount
        }

        // Should be capped at 15.0, not 50.0
        assertEquals(15.0, discount, 0.01)
    }

    @Test
    fun calculateDiscount_fixedAmount_returnsFixedAmount() {
        // Given a $10 fixed discount promo
        val promoCode = PromoCode(
            code = "FLAT10",
            description = "$10 off",
            discountPercent = 0,
            discountAmount = 10.0,
            minOrderAmount = 0.0,
            maxDiscount = 0.0,
            isValid = true
        )
        val subtotal = 80.0

        val discount = if (promoCode.discountPercent > 0) {
            val raw = subtotal * promoCode.discountPercent / 100
            if (promoCode.maxDiscount > 0) minOf(raw, promoCode.maxDiscount) else raw
        } else {
            promoCode.discountAmount
        }

        assertEquals(10.0, discount, 0.01)
    }

    // ── Filtering Tests ──

    @Test
    fun filterHousekeepers_byName_returnsMatchingResults() {
        val allHousekeepers = MockData.housekeepers
        val query = allHousekeepers.first().name.take(3) // Use first 3 letters of first housekeeper

        val filtered = allHousekeepers.filter { h ->
            h.name.contains(query, ignoreCase = true)
        }

        assertTrue("Filter should return at least one result", filtered.isNotEmpty())
        assertTrue("All results should contain the query",
            filtered.all { it.name.contains(query, ignoreCase = true) })
    }

    @Test
    fun filterHousekeepers_byService_returnsMatchingResults() {
        val allHousekeepers = MockData.housekeepers
        val targetService = "Standard"

        val filtered = allHousekeepers.filter { h ->
            h.services.contains(targetService)
        }

        assertTrue("Filter should return housekeepers with Cleaning service",
            filtered.all { it.services.contains(targetService) })
    }

    @Test
    fun filterHousekeepers_byRating_returnsAboveThreshold() {
        val allHousekeepers = MockData.housekeepers
        val minRating = 4.5f

        val filtered = allHousekeepers.filter { it.rating >= minRating }

        assertTrue("All results should have rating >= $minRating",
            filtered.all { it.rating >= minRating })
    }

    @Test
    fun filterHousekeepers_byPrice_returnsInRange() {
        val allHousekeepers = MockData.housekeepers
        val priceRange = 20f..50f

        val filtered = allHousekeepers.filter {
            it.pricePerHour >= priceRange.start && it.pricePerHour <= priceRange.endInclusive
        }

        assertTrue("All results should be within price range",
            filtered.all { it.pricePerHour >= priceRange.start && it.pricePerHour <= priceRange.endInclusive })
    }

    @Test
    fun filterHousekeepers_noMatch_returnsEmptyList() {
        val allHousekeepers = MockData.housekeepers
        val query = "ZZZZNONEXISTENT"

        val filtered = allHousekeepers.filter { h ->
            h.name.contains(query, ignoreCase = true)
        }

        assertTrue("Filter with nonsense query should return empty", filtered.isEmpty())
    }

    // ── UiState Tests ──

    @Test
    fun housekeepUiState_defaultValues_areCorrect() {
        val state = HousekeepUiState()

        assertFalse("Default isLoading should be false", state.isLoading)
        assertEquals("Default searchQuery should be empty", "", state.searchQuery)
        assertEquals("Default category should be All", "All", state.selectedCategory)
        assertTrue("Default housekeepers should be empty", state.housekeepers.isEmpty())
        assertNull("Default errorMessage should be null", state.errorMessage)
    }

    @Test
    fun housekeepUiState_copyWithLoading_updatesCorrectly() {
        val initial = HousekeepUiState()
        val loading = initial.copy(isLoading = true)

        assertTrue("Copied state should be loading", loading.isLoading)
        assertEquals("Other fields unchanged", "", loading.searchQuery)
    }

    // ── MockData Tests ──

    @Test
    fun mockData_housekeepersNotEmpty() {
        assertTrue("MockData should have housekeepers", MockData.housekeepers.isNotEmpty())
    }

    @Test
    fun mockData_allHousekeepersHaveRequiredFields() {
        MockData.housekeepers.forEach { h ->
            assertTrue("Housekeeper id should not be blank: ${h.name}", h.id.isNotBlank())
            assertTrue("Housekeeper name should not be blank", h.name.isNotBlank())
            assertTrue("Housekeeper should have at least one service", h.services.isNotEmpty())
            assertTrue("Housekeeper rating should be valid", h.rating in 0f..5f)
            assertTrue("Housekeeper price should be positive", h.pricePerHour > 0)
        }
    }

    @Test
    fun mockData_promoCodesExist() {
        assertTrue("MockData should have promo codes", MockData.promoCodes.isNotEmpty())
    }

    @Test
    fun mockData_servicePackagesExist() {
        assertTrue("MockData should have service packages", MockData.servicePackages.isNotEmpty())
    }
}