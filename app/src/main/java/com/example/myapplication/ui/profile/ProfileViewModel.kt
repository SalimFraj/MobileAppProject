package com.example.myapplication.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.HouseKeepApplication
import com.example.myapplication.data.HousekeeperRepository
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.data.UserPreferencesRepository
import com.example.myapplication.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for user profile, settings, and address management.
 * Used by ProfileScreen, AddressManagementScreen, WalletScreen, and SettingsScreen.
 */
class ProfileViewModel(
    application: Application,
    private val repository: HousekeeperRepository,
    private val preferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

    // Dark Mode
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    // User Preferences from DataStore
    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    // User Profile via Repository
    val userProfile: StateFlow<UserProfileEntity?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Saved Addresses via Repository
    val addresses: StateFlow<List<AddressEntity>> = repository.getAllAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDarkMode() {
        _isDarkMode.value = when (_isDarkMode.value) {
            true -> false
            false -> true
            else -> true
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = if (current != null) {
                current.copy(name = name, email = email, phone = phone)
            } else {
                UserProfileEntity(name = name, email = email, phone = phone)
            }
            repository.updateUserProfile(updated)
        }
    }

    // Address Management
    fun addAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.insertAddress(address)
        }
    }

    fun deleteAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.deleteAddress(address)
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            repository.setDefaultAddress(addressId)
        }
    }

    // ── Preferences / Settings ──

    fun completeOnboarding() {
        viewModelScope.launch { preferencesRepository.completeOnboarding() }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            val current = preferences.value.notificationsEnabled
            preferencesRepository.setNotificationsEnabled(!current)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { preferencesRepository.setLanguage(language) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            preferencesRepository.clearAll()
            // Re-mark onboarding as complete so user isn't sent back
            preferencesRepository.completeOnboarding()
        }
    }

    // Initialize default user profile if not exists
    init {
        viewModelScope.launch {
            if (userProfile.value == null) {
                repository.updateUserProfile(
                    UserProfileEntity(
                        name = "Salim Al-Harthy",
                        email = "salim@example.com",
                        referralCode = "SALIM25",
                        loyaltyPoints = 150
                    )
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as HouseKeepApplication)
                val repository = application.container.housekeeperRepository
                val prefsRepo = application.container.userPreferencesRepository
                ProfileViewModel(
                    application = application,
                    repository = repository,
                    preferencesRepository = prefsRepo
                )
            }
        }
    }
}
