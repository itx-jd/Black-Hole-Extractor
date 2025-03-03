package com.blackhole.downloaders.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class PermissionUtils {

    public static boolean isPostNotificationsPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Below Android 13, no need for this permission
    }

    public static boolean isExternalStoragePermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // Android 9 and below
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 10+ uses Media Store, no permission needed for public directories
    }

    public static boolean hasRequiredPermissions(Context context) {
        return isPostNotificationsPermissionGranted(context) && isExternalStoragePermissionGranted(context);
    }
}