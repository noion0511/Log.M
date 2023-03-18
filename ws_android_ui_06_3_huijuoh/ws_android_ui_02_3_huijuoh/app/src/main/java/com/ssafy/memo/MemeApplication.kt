package com.ssafy.memo

import android.app.Application

class MemeApplication : Application() {

    companion object {
        lateinit var instance: MemeApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}