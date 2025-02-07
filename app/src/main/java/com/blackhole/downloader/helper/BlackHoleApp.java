package com.blackhole.downloader.helper;

import android.app.Application;
import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

public class BlackHoleApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this; // Store the app context for global access
        // Initialize Firebase Analytics
        FirebaseAnalytics.getInstance(this);
    }


    public static Context getContext() {
        return context;
    }
}

