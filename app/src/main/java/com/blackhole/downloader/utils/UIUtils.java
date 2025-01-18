package com.blackhole.downloader.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class UIUtils {

    public static void delayedVisibility(View... views) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            for (View view : views) {
                AnimationUtils.fadeInView(view);
            }
        }, 1000);
    }
}
