package com.zxw.screenshot_master

import android.app.Application

/*
* 提供全文context对象
* */
class ScreenShotApplication : Application() {

    companion object {
        lateinit var applicationContext: Application
    }

    override fun onCreate() {
        super.onCreate()
        ScreenShotApplication.applicationContext = this
    }
}