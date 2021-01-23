package com.example.pluginexercise;

import android.app.Application;

import com.example.core.PluginManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PluginManager.getInstance(this).init();
    }
}
