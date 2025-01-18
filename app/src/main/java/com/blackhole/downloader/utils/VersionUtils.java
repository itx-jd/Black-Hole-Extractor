package com.blackhole.downloader.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class VersionUtils {

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
}
