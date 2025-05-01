package com.blackhole.downloaders.helper;

import android.app.Application;
import android.content.Context;


public class BlackHoleApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this; // Store the app context for global access
    }


    public static Context getContext() {
        return context;
    }
}

