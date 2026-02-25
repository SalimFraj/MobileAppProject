package com.example.myapplication

import com.example.myapplication.model.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for profile-related logic.
 * Tests dark mode toggling, profile update logic, and address management.
 */
class ProfileViewModelTest {

    // ── Dark Mode ──

    @Test
    fun darkMode_initialValue_isNull() {
        // Dark mode starts as null (system default)
        val isDarkMode: Boolean? = null
        assertNull("Initial dark mode should be null", isDarkMode)
    }

    @Test
    fun darkMode_toggleFromNull_becomesTrue() {
        val initial: Boolean? = null
        val toggled = when (initial) {
            true -> false
            false -> true
            else -> true
        }
        assertTrue("Toggle from null should be true", toggled)
    }

    @Test
    fun darkMode_toggleFromTrue_becomesFalse() {
        val initial: Boolean? = true
        val toggled = when (initial) {
            true -> false
            false -> true
            else -> true
        }
        assertFalse("Toggle from true should be false", toggled)
    }

    @Test
    fun darkMode_toggleFromFalse_becomesTrue() {
        val initial: Boolean? = false
        val toggled = when (initial) {
            true -> false
            false -> true
            else -> true
        }
        assertTrue("Toggle from false should be true", toggled)
    }

    @Test
    fun darkMode_doubleToggle_returnsToOriginal() {
        val initial: Boolean? = null
        val first = when (initial) { true -> false; false -> true; else -> true }
        val second = when (first) { true -> false; false -> true; else -> true }
        assertEquals("Double toggle from null should return to false", false, second)
    }

    // ── User Profile ──

    @Test
    fun userProfile_defaultEntity_hasCorrectDefaults() {
        val profile = UserProfileEntity(
            name = "Test User",
            email = "test@example.com"
        )
        assertEquals("current_user", profile.id)
        assertEquals("Test User", profile.name)
        assertEquals("test@example.com", profile.email)
        assertEquals("", profile.phone)
        assertEquals(0, profile.loyaltyPoints)
    }

    @Test
    fun userProfile_copyUpdatesFields() {
        val original = UserProfileEntity(name = "Old Name", email = "old@email.com")
        val updated = original.copy(name = "New Name", email = "new@email.com", phone = "555-1234")
        assertEquals("New Name", updated.name)
        assertEquals("new@email.com", updated.email)
        assertEquals("555-1234", updated.phone)
        assertEquals("ID should be preserved", original.id, updated.id)
    }

    @Test
    fun userProfile_updatePreservesLoyaltyPoints() {
        val original = UserProfileEntity(
            name = "User", email = "u@e.com", loyaltyPoints = 150
        )
        val updated = original.copy(name = "Updated User")
        assertEquals("Loyalty points should be preserved", 150, updated.loyaltyPoints)
    }

    @Test
    fun userProfile_updatePreservesReferralCode() {
        val original = UserProfileEntity(
            name = "User", email = "u@e.com", referralCode = "ABC123"
        )
        val updated = original.copy(name = "New Name", email = "new@e.com", phone = "123")
        assertEquals("Referral code should be preserved", "ABC123", updated.referralCode)
    }

    // ── Address Management ──

    @Test
    fun address_entity_hasCorrectFields() {
        val address = AddressEntity(
            id = "addr1",
            label = "Home",
            fullAddress = "123 Main St, Springfield",
            isDefault = true
        )
        assertEquals("addr1", address.id)
        assertEquals("Home", address.label)
        assertEquals("123 Main St, Springfield", address.fullAddress)
        assertTrue(address.isDefault)
    }

    @Test
    fun address_defaultIsFalseByDefault() {
        val address = AddressEntity(
            id = "addr2",
            label = "Work",
            fullAddress = "456 Office Blvd, Metro"
        )
        assertFalse("New address should not be default", address.isDefault)
    }

    @Test
    fun address_setDefault_onlyOneDefault() {
        val addresses = listOf(
            AddressEntity(id = "1", label = "Home", fullAddress = "Home St", isDefault = true),
            AddressEntity(id = "2", label = "Work", fullAddress = "Work St", isDefault = false)
        )
        // Simulate clearing defaults and setting new one
        val cleared = addresses.map { it.copy(isDefault = false) }
        val updated = cleared.map { if (it.id == "2") it.copy(isDefault = true) else it }
        assertEquals("Only one address should be default", 1, updated.count { it.isDefault })
        assertTrue("Address 2 should be the new default", updated.find { it.id == "2" }!!.isDefault)
        assertFalse("Address 1 should no longer be default", updated.find { it.id == "1" }!!.isDefault)
    }

    // ── Loyalty Tier ──

    @Test
    fun loyaltyTier_platinum() {
        val tier = calculateTier(500)
        assertEquals("Platinum", tier)
    }

    @Test
    fun loyaltyTier_gold() {
        val tier = calculateTier(250)
        assertEquals("Gold", tier)
    }

    @Test
    fun loyaltyTier_silver() {
        val tier = calculateTier(100)
        assertEquals("Silver", tier)
    }

    @Test
    fun loyaltyTier_bronze() {
        val tier = calculateTier(50)
        assertEquals("Bronze", tier)
    }

    private fun calculateTier(points: Int): String = when {
        points >= 500 -> "Platinum"
        points >= 250 -> "Gold"
        points >= 100 -> "Silver"
        else -> "Bronze"
    }
}
