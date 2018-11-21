package com.edit.image.sample;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * @author y
 * @create 2018/11/21
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
