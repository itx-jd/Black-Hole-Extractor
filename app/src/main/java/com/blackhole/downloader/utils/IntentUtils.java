package com.blackhole.downloader.utils;

import android.content.Intent;

public class IntentUtils {

    public static String extractSharedText(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            return intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        return null;
    }
}
