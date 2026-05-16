package com.powergrid.exemployee.common

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val scale  = FontPrefs.getScale(newBase)
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = scale
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}
