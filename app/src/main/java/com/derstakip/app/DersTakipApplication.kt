package com.derstakip.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DersTakipApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
