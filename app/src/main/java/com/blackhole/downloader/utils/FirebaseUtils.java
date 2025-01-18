package com.blackhole.downloader.utils;

import android.os.Bundle;
import android.util.Log;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    public static void logEvent(FirebaseAnalytics firebaseAnalytics, String id, String name) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

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
}
