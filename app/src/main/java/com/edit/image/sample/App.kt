package com.edit.image.sample

import android.app.Application

import com.squareup.leakcanary.LeakCanary

/**
 * @author y
 * @create 2018/11/21
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
    }
}
