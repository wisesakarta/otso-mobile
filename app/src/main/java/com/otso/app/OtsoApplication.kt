package com.otso.app

import android.app.Application
import com.otso.app.core.NeuralVisionEngine

class OtsoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NeuralVisionEngine.loadModel(this)
    }
}
