package com.openai.taskfall.platform

import android.app.Application
import com.openai.taskfall.mainApp
import com.pico.spatial.ui.foundation.dsl.launch

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
