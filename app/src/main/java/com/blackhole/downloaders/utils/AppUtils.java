package com.blackhole.downloaders.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.util.Log;

import com.blackhole.downloaders.ui.MainActivity;

public class AppUtils {
    public static String RAPID_API_KEY = "";
    public static void restartApp(Context context) {
        // Intent to restart the activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // Clear activity stack
        context.startActivity(intent);
        // Kill the current process
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
