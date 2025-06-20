package com.rodolfo.itaxcix

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, "AIzaSyBFVuOButa5EMduTqE4_iis8T6yKyhdpvI")
    }
}
