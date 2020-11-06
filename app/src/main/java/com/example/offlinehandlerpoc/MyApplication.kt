package com.example.offlinehandlerpoc

import android.app.Application
import android.content.Context

class MyApplication: Application() {
    private lateinit var context: Context
    override fun onCreate(){
        super.onCreate()
        this.context = applicationContext
    }

    fun getAppContext(): Context{
        return this.context
    }
}