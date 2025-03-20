package com.blackhole.downloaders.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.util.Log;

public class FirebaseApiManager {
    private static final String TAG = "FirebaseApiManager";
    private static FirebaseApiManager instance;
    private DatabaseReference databaseReference;

    private String rapid_api ="";
    private String terabox_api ="";

    // Callback interface
    public interface ApiKeyCallback {
        void onApiKeysLoaded(String rapid_api,String terabox_api);
        void onError(String error);
    }

    private FirebaseApiManager() {
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("api_keys");
    }

    // Singleton pattern
    public static synchronized FirebaseApiManager getInstance() {
        if (instance == null) {
            instance = new FirebaseApiManager();
        }
        return instance;
    }

    // Fetch API keys from Firebase
    public void fetchApiKeys(ApiKeyCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    rapid_api = dataSnapshot
                            .child("rapid_api")
                            .getValue(String.class);

                    terabox_api = dataSnapshot
                            .child("terabox_api")
                            .getValue(String.class);


                    if (rapid_api != null && terabox_api != null) {
                        callback.onApiKeysLoaded(rapid_api,terabox_api);
                    } else {
                        callback.onError("API keys not found in Firebase");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing API keys: " + e.getMessage());
                    callback.onError("Error parsing API keys: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase error: " + databaseError.getMessage());
                callback.onError("Firebase error: " + databaseError.getMessage());
            }
        });
    }

    // Getter methods for direct access (optional, if needed after initial load)
    public String getRapidApiKey() {
        return rapid_api;
    }

    public String getTeraboxApiKey() {
        return terabox_api;
    }
}