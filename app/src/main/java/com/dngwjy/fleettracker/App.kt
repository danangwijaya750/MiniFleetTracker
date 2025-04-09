package com.dngwjy.fleettracker

import android.app.Application
import com.dngwjy.fleettracker.utils.logE


//@HiltAndroidApp
class App:Application() {
    override fun onCreate() {
        super.onCreate()
        logE("start")
    }
}