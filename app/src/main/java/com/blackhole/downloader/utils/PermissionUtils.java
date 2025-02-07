package com.blackhole.downloader.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

public class PermissionUtils {

    public static boolean isPostNotificationsPermissionGranted(Context context) {
        // Check for Android version (POST_NOTIFICATIONS is only applicable from Android 13 onwards)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the permission is granted
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // For Android versions below 13, return true as the permission is not required
        return true;
    }

    public static boolean isExternalStoragePermissionGranted(Context context) {
        // For Android 11 and above, check if external storage access is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true; // For older versions, assume it's possible
    }
}
