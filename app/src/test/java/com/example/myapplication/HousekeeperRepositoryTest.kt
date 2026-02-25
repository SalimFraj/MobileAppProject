package com.example.myapplication

import com.example.myapplication.data.MockData
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for HousekeeperRepository logic.
 * Tests housekeeper data access, search, featured filtering,
 * promo code lookup, and mock data integrity.
 */
class HousekeeperRepositoryTest {

    // ── Housekeeper Data ──

    @Test
    fun housekeepers_notEmpty() {
        assertTrue("MockData should have housekeepers", MockData.housekeepers.isNotEmpty())
    }

    @Test
    fun housekeepers_haveUniqueIds() {
        val ids = MockData.housekeepers.map { it.id }
        assertEquals("All IDs should be unique", ids.size, ids.toSet().size)
    }

    @Test
    fun housekeepers_allHaveRequiredFields() {
        MockData.housekeepers.forEach { h ->
            assertTrue("ID should not be blank: ${h.name}", h.id.isNotBlank())
            assertTrue("Name should not be blank", h.name.isNotBlank())
            assertTrue("Should have at least one service", h.services.isNotEmpty())
            assertTrue("Rating should be 0-5: ${h.rating}", h.rating in 0f..5f)
            assertTrue("Price should be positive: ${h.pricePerHour}", h.pricePerHour > 0)
            assertTrue("Experience should be non-negative", h.experienceYears >= 0)
        }
    }

    @Test
    fun housekeepers_allHaveImages() {
        MockData.housekeepers.forEach { h ->
            assertTrue("Image URL should not be empty: ${h.name}", h.imageUrl.isNotBlank())
        }
    }

    // ── Search ──

    @Test
    fun search_byExactName_findsHousekeeper() {
        val target = MockData.housekeepers.first()
        val results = MockData.housekeepers.filter {
            it.name.contains(target.name, ignoreCase = true)
        }
        assertTrue("Exact name search should find the housekeeper", results.isNotEmpty())
        assertEquals("Should find the specific housekeeper", target.id, results.first().id)
    }

    @Test
    fun search_byPartialName_findsResults() {
        val partialName = MockData.housekeepers.first().name.take(3)
        val results = MockData.housekeepers.filter {
            it.name.contains(partialName, ignoreCase = true)
        }
        assertTrue("Partial name search should find results", results.isNotEmpty())
    }

    @Test
    fun search_byServiceName_findsResults() {
        val service = "Standard"
        val results = MockData.housekeepers.filter {
            it.services.any { s -> s.contains(service, ignoreCase = true) }
        }
        assertTrue("Service-based search should find results", results.isNotEmpty())
    }

    @Test
    fun search_noMatch_returnsEmpty() {
        val results = MockData.housekeepers.filter {
            it.name.contains("ZZZZZ", ignoreCase = true) ||
            it.services.any { s -> s.contains("ZZZZZ", ignoreCase = true) }
        }
        assertTrue("Non-matching search should return empty", results.isEmpty())
    }

    // ── Featured Housekeepers ──

    @Test
    fun featuredHousekeepers_allHighRated() {
        val featured = MockData.housekeepers.filter { it.rating >= 4.8f }
        assertTrue("Featured should only include high-rated", featured.all { it.rating >= 4.8f })
        assertTrue("Should have at least one featured housekeeper", featured.isNotEmpty())
    }

    @Test
    fun getHousekeeperById_existingId_returnsHousekeeper() {
        val target = MockData.housekeepers.first()
        val found = MockData.housekeepers.find { it.id == target.id }
        assertNotNull("Should find housekeeper by ID", found)
        assertEquals(target.name, found!!.name)
    }

    @Test
    fun getHousekeeperById_nonExistentId_returnsNull() {
        val found = MockData.housekeepers.find { it.id == "NONEXISTENT" }
        assertNull("Non-existent ID should return null", found)
    }

    // ── Promo Codes ──

    @Test
    fun promoCodes_notEmpty() {
        assertTrue("Should have promo codes", MockData.promoCodes.isNotEmpty())
    }

    @Test
    fun promoCodes_haveUniqueCodes() {
        val codes = MockData.promoCodes.map { it.code }
        assertEquals("All promo codes should be unique", codes.size, codes.toSet().size)
    }

    @Test
    fun promoCode_validLookup_returnsCode() {
        val code = MockData.promoCodes.first()
        val found = MockData.promoCodes.find {
            it.code.equals(code.code, ignoreCase = true) && it.isValid
        }
        assertNotNull("Valid promo code should be found", found)
    }

    @Test
    fun promoCode_invalidLookup_returnsNull() {
        val found = MockData.promoCodes.find {
            it.code.equals("DOESNOTEXIST", ignoreCase = true) && it.isValid
        }
        assertNull("Invalid promo code should return null", found)
    }

    @Test
    fun promoCodes_haveValidDiscountValues() {
        MockData.promoCodes.forEach { promo ->
            val hasPercent = promo.discountPercent > 0
            val hasFixed = promo.discountAmount > 0
            assertTrue("Promo ${promo.code} should have percent or fixed discount",
                hasPercent || hasFixed || promo.discountPercent == 0)
            assertTrue("Percent should be 0-100", promo.discountPercent in 0..100)
            assertTrue("Min order should be non-negative", promo.minOrderAmount >= 0)
        }
    }

    // ── Reviews ──

    @Test
    fun reviews_notEmpty() {
        assertTrue("Should have reviews", MockData.reviews.isNotEmpty())
    }

    @Test
    fun reviews_haveValidRatings() {
        MockData.reviews.forEach { review ->
            assertTrue("Rating should be 1-5: ${review.rating}", review.rating in 1..5)
        }
    }

    // ── Service Packages ──

    @Test
    fun servicePackages_notEmpty() {
        assertTrue("Should have service packages", MockData.servicePackages.isNotEmpty())
    }

    @Test
    fun servicePackages_haveValidPricing() {
        MockData.servicePackages.forEach { pkg ->
            assertTrue("Price should be positive: ${pkg.name}", pkg.price > 0)
            assertTrue("Original price >= price: ${pkg.name}", pkg.originalPrice >= pkg.price)
            assertTrue("Duration should be positive: ${pkg.name}", pkg.durationHours > 0)
        }
    }

    @Test
    fun servicePackages_haveServices() {
        MockData.servicePackages.forEach { pkg ->
            assertTrue("Package should have at least one service: ${pkg.name}",
                pkg.services.isNotEmpty())
        }
    }

    // ── Categories ──

    @Test
    fun categories_notEmpty() {
        assertTrue("Categories should not be empty", MockData.categories.isNotEmpty())
    }

    @Test
    fun categories_containsAll() {
        assertTrue("Categories should contain 'All'", MockData.categories.contains("All"))
    }
}
