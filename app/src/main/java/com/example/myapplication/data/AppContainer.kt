package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.local.AppDatabase

/**
 * Dependency injection container at the application level.
 * Provides repository instances to the rest of the app.
 */
interface AppContainer {
    val housekeeperRepository: HousekeeperRepository
}

/**
 * Default implementation of [AppContainer] that provides real dependencies.
 * Creates the database and repository once, then shares them across the app.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val housekeeperRepository: HousekeeperRepository by lazy {
        HousekeeperRepository(database)
    }
}
