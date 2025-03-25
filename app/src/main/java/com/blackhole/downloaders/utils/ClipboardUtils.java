package com.blackhole.downloaders.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

public class ClipboardUtils {

    public static String getClipboardText(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                return clipData.getItemAt(0).getText().toString();
            }
        }
        return "";
    }

    public static boolean isURLInClipboard(String clipboardText) {
        return clipboardText.startsWith("https://") || clipboardText.startsWith("http://");
    }


    // Return empty string if null or empty
    public static String getClipBoardLink(Context context) {
        try {
            // Get the clipboard text
            String clipboardText = getClipboardText(context);

            // Check if the clipboard text is null or empty
            if (clipboardText == null || clipboardText.isEmpty()) {
                return "";
            }
            // Check if the clipboard contains a valid URL
            if (ClipboardUtils.isURLInClipboard(clipboardText)) {
                return clipboardText;
            } else {
                return "";
            }

        } catch (Exception e) {
            // Catch any unexpected exceptions and log them
            e.printStackTrace();
            return "";
        }
    }
}
