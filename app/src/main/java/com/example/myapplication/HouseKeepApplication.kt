package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppContainer
import com.example.myapplication.data.DefaultAppContainer

/**
 * Custom Application class that initializes the dependency injection container.
 * Registered in AndroidManifest.xml via android:name=".HouseKeepApplication".
 */
class HouseKeepApplication : Application() {

    /** AppContainer instance used by the rest of the app to obtain dependencies */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
