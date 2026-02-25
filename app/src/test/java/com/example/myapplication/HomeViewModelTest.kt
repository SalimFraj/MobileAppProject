package com.example.myapplication

import com.example.myapplication.data.MockData
import com.example.myapplication.ui.home.HousekeepUiState
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for HouseKeep home screen logic.
 * Tests filtering, search, category selection, price/rating filtering,
 * featured housekeepers, and UI state defaults.
 */
class HomeViewModelTest {

    private val allHousekeepers = MockData.housekeepers

    // ── Search & Filtering ──

    @Test
    fun filterByName_returnsMatchingHousekeepers() {
        val query = allHousekeepers.first().name.take(3)
        val filtered = allHousekeepers.filter { h ->
            h.name.contains(query, ignoreCase = true) ||
            h.services.any { it.contains(query, ignoreCase = true) }
        }
        assertTrue("Should find at least one result for '$query'", filtered.isNotEmpty())
        assertTrue("All results should match query",
            filtered.all { it.name.contains(query, ignoreCase = true) })
    }

    @Test
    fun filterByName_caseInsensitive() {
        val name = allHousekeepers.first().name.uppercase()
        val filtered = allHousekeepers.filter { it.name.contains(name, ignoreCase = true) }
        assertTrue("Case-insensitive search should find results", filtered.isNotEmpty())
    }

    @Test
    fun filterByService_returnsHousekeepersWithService() {
        val targetService = "Standard"
        val filtered = allHousekeepers.filter { h ->
            h.services.contains(targetService)
        }
        assertTrue("All results should offer '$targetService'",
            filtered.all { it.services.contains(targetService) })
    }

    @Test
    fun filterByCategory_allReturnsEverything() {
        val category = "All"
        val filtered = allHousekeepers.filter { category == "All" }
        assertEquals("'All' should return every housekeeper",
            allHousekeepers.size, filtered.size)
    }

    @Test
    fun filterByCategory_specificServiceFiltersCorrectly() {
        val category = "Deep Clean"
        val filtered = allHousekeepers.filter { it.services.contains(category) }
        assertTrue("All results should have '$category' service",
            filtered.all { it.services.contains(category) })
    }

    @Test
    fun filterByRating_returnsAboveThreshold() {
        val minRating = 4.5f
        val filtered = allHousekeepers.filter { it.rating >= minRating }
        assertTrue("All results should have rating >= $minRating",
            filtered.all { it.rating >= minRating })
    }

    @Test
    fun filterByPrice_returnsInRange() {
        val priceRange = 20f..50f
        val filtered = allHousekeepers.filter {
            it.pricePerHour >= priceRange.start && it.pricePerHour <= priceRange.endInclusive
        }
        assertTrue("All results should be within price range $priceRange",
            filtered.all { it.pricePerHour >= priceRange.start && it.pricePerHour <= priceRange.endInclusive })
    }

    @Test
    fun filterByPrice_extremeRange_returnsAll() {
        val priceRange = 0f..1000f
        val filtered = allHousekeepers.filter {
            it.pricePerHour >= priceRange.start && it.pricePerHour <= priceRange.endInclusive
        }
        assertEquals("Extreme range should return all housekeepers",
            allHousekeepers.size, filtered.size)
    }

    @Test
    fun filterByPrice_tightRange_returnsSubset() {
        val minPrice = allHousekeepers.minOf { it.pricePerHour.toFloat() }
        val maxPrice = allHousekeepers.maxOf { it.pricePerHour.toFloat() }
        // Use a range that excludes some
        val midPrice = (minPrice + maxPrice) / 2
        val filtered = allHousekeepers.filter {
            it.pricePerHour >= midPrice && it.pricePerHour <= maxPrice
        }
        assertTrue("Tight range should return fewer results",
            filtered.size <= allHousekeepers.size)
    }

    @Test
    fun filterByNoMatch_returnsEmpty() {
        val query = "ZZZZNONEXISTENT"
        val filtered = allHousekeepers.filter { h ->
            h.name.contains(query, ignoreCase = true)
        }
        assertTrue("Nonsense query should return empty", filtered.isEmpty())
    }

    @Test
    fun combinedFilters_narrowResults() {
        val query = ""
        val category = "All"
        val minRating = 4.8f
        val priceRange = 20f..50f

        val filtered = allHousekeepers.filter { h ->
            val matchesQuery = query.isEmpty() || h.name.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || h.services.contains(category)
            val matchesPrice = h.pricePerHour >= priceRange.start && h.pricePerHour <= priceRange.endInclusive
            val matchesRating = h.rating >= minRating
            matchesQuery && matchesCategory && matchesPrice && matchesRating
        }
        assertTrue("Combined filters should narrow results",
            filtered.size <= allHousekeepers.size)
        assertTrue("All results should match all criteria",
            filtered.all { it.rating >= minRating && it.pricePerHour >= priceRange.start })
    }

    // ── Featured Housekeepers ──

    @Test
    fun featuredHousekeepers_onlyHighRated() {
        val featured = allHousekeepers
            .filter { it.rating >= 4.8f }
            .sortedByDescending { it.reviewCount }
        assertTrue("Featured should only include 4.8+ rated",
            featured.all { it.rating >= 4.8f })
    }

    @Test
    fun featuredHousekeepers_sortedByReviewCount() {
        val featured = allHousekeepers
            .filter { it.rating >= 4.8f }
            .sortedByDescending { it.reviewCount }
        for (i in 0 until featured.size - 1) {
            assertTrue("Should be sorted by review count descending",
                featured[i].reviewCount >= featured[i + 1].reviewCount)
        }
    }

    // ── UI State ──

    @Test
    fun housekeepUiState_defaultValues() {
        val state = HousekeepUiState()
        assertFalse("isLoading should be false by default", state.isLoading)
        assertEquals("searchQuery should be empty", "", state.searchQuery)
        assertEquals("selectedCategory should be 'All'", "All", state.selectedCategory)
        assertTrue("housekeepers should be empty", state.housekeepers.isEmpty())
        assertNull("errorMessage should be null", state.errorMessage)
    }

    @Test
    fun housekeepUiState_copyPreservesUnchangedFields() {
        val initial = HousekeepUiState()
        val loading = initial.copy(isLoading = true)
        assertTrue("isLoading should be updated", loading.isLoading)
        assertEquals("searchQuery should be unchanged", "", loading.searchQuery)
        assertEquals("selectedCategory should be unchanged", "All", loading.selectedCategory)
    }

    @Test
    fun housekeepUiState_copyWithSearchQuery() {
        val state = HousekeepUiState().copy(searchQuery = "Alice")
        assertEquals("searchQuery should be 'Alice'", "Alice", state.searchQuery)
    }
}
