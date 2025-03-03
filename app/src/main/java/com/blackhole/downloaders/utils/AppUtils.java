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

    public static void restartApp(Context context) {
        // Intent to restart the activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // Clear activity stack
        context.startActivity(intent);
        // Kill the current process
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    public static boolean isVersionOutdated(Context context, String latestVersion) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String currentVersion = packageInfo.versionName;

            Double currentVersionDouble = Double.parseDouble(currentVersion);
            Double latestVersionDouble = Double.parseDouble(latestVersion);

            return currentVersionDouble < latestVersionDouble;

        } catch (PackageManager.NameNotFoundException | NumberFormatException e) {
            Log.e("VersionUtils", "Error comparing versions", e);
            return true; // Assume outdated if an error occurs
        }
    }

    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "WiFi";
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return "Mobile Data";
                }
            }
        }
        return "Unknown";
    }

    // Helper Method: Get Battery Percentage
    public static int getBatteryPercentage(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (bm != null) {
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return -1; // Unknown battery level
    }

    public static String getLocalAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Failed To Fetch App Version";
        }
    }

}
