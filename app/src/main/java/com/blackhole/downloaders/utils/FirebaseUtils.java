package com.blackhole.downloaders.utils;

import static com.blackhole.downloaders.utils.AppUtils.getBatteryPercentage;
import static com.blackhole.downloaders.utils.AppUtils.getNetworkType;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.blackhole.downloaders.model.ReportedUrl;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    public static void getAppVersion(DatabaseReference reference, FirebaseCallback callback) {
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(task.getResult().getValue(String.class));
            } else {
                callback.onFailure(task.getException());
                Log.e("FirebaseUtils", "Error fetching app version", task.getException());
            }
        });
    }

    public interface FirebaseCallback {
        void onSuccess(String result);
        void onFailure(Exception e);
    }

    // save url to firebase
    public static void reportUrlToFirebase(Context context, String url) {

        long timestamp = System.currentTimeMillis();

        // Collect Device Details
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        String androidVersion = Build.VERSION.RELEASE;
        String networkType = getNetworkType(context);
        int batteryPercentage = getBatteryPercentage(context);
        String timeZone = java.util.TimeZone.getDefault().getID();
        String appVersion = AppUtils.getLocalAppVersion(context);

        // Create a ReportedUrl object
        ReportedUrl reportedUrl = new ReportedUrl(url, timestamp, deviceModel, androidVersion, appVersion, networkType, batteryPercentage, timeZone);

        // Store Data in Firebase
        DatabaseReference reportedUrlReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("failed_url")
                .child(String.valueOf(timestamp));

        reportedUrlReference.setValue(reportedUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseUtils", "Reported URL to Firebase with details: " + reportedUrl);
            } else {
                Log.e("FirebaseUtils", "Failed to report URL to Firebase", task.getException());
            }
        });
    }

}
