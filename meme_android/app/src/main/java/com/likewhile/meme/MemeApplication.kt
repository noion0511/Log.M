package com.likewhile.meme

import android.app.Application
import android.content.res.Configuration
import java.util.*

class MemeApplication : Application() {

    companion object {
        lateinit var instance: MemeApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val lang = Locale.getDefault().language
        val country = Locale.getDefault().country

        val config = Configuration(resources.configuration)
        when (lang) {
            "ko" -> {
                config.setLocale(Locale.KOREAN)
            }
            "zh" -> {
                if (country == "CN") {
                    config.setLocale(Locale.SIMPLIFIED_CHINESE)
                } else {
                    config.setLocale(Locale.TRADITIONAL_CHINESE)
                }
            }
            "ja" -> {
                config.setLocale(Locale.JAPANESE)
            }
            "es" -> {
                config.setLocale(Locale("es"))
            }
            else -> {
                config.setLocale(Locale.ENGLISH)
            }
        }

        val context = createConfigurationContext(config)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}