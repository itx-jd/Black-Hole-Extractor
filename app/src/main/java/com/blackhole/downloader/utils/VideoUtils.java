package com.blackhole.downloader.utils;

import static com.blackhole.downloader.constants.Constant.isURlValid;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.blackhole.downloader.R;
import com.blackhole.downloader.callback.Callback;
import com.blackhole.downloader.constants.Constant;
import com.blackhole.downloader.detector.PlatformDetector;
import com.blackhole.downloader.fetcher.VideoDataFetcher;
import com.blackhole.downloader.fetcher.VideoDataFetcherFactory;
import com.blackhole.downloader.ui.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class VideoUtils {

    public static void fetchVideoData(Context context, LinearProgressIndicator progressBar, TextView tvWait, String url) {
         // when user use send intent we use intent url
        if (isURlValid(url)) {
            processVideoUrl(context, progressBar, tvWait, url);
        } else if (url.isEmpty()) {
            // when user press button from home screen we use Clipboard url
            handleEmptyUrl(context, progressBar, tvWait);
        } else {
            Toast.makeText(context, "Invalid Video Url", Toast.LENGTH_SHORT).show();
            MainActivity.downloadFroze = false;
        }
    }
    private static void handleEmptyUrl(Context context, LinearProgressIndicator progressBar, TextView tvWait) {
        try {
            // Get the clipboard text
            String clipboardText = ClipboardUtils.getClipboardText(context);

            // Check if the clipboard text is null or empty
            if (clipboardText == null || clipboardText.isEmpty()) {
                Toast.makeText(context, "Copy video link first", Toast.LENGTH_SHORT).show();
                MainActivity.downloadFroze = false;
                return;
            }

            // Check if the clipboard contains a valid URL
            if (ClipboardUtils.isURLInClipboard(clipboardText)) {
                processVideoUrl(context, progressBar, tvWait, clipboardText);
            } else {
                Toast.makeText(context, "Copy video link first", Toast.LENGTH_SHORT).show();
                MainActivity.downloadFroze = false;
            }

        } catch (Exception e) {
            // Catch any unexpected exceptions and log them
            e.printStackTrace();
            Toast.makeText(context, "Copy video link first", Toast.LENGTH_SHORT).show();
            MainActivity.downloadFroze = false;
        }
    }


    private static void processVideoUrl(Context context, LinearProgressIndicator progressBar, TextView tvWait, String url) {

        tvWait.setVisibility(View.VISIBLE);
        String platformName = PlatformDetector.getPlatformName(url);
        Toast.makeText(context, "BlackHole Pasted From " + platformName, Toast.LENGTH_SHORT).show();

        VideoDataFetcher fetcher = VideoDataFetcherFactory.getFetcher(platformName);
        fetcher.fetchVideoData(url, new Callback() {
            @Override
            public void onResult(String[] result) {
                handleFetchResult(context, progressBar, tvWait, result);
            }
        });
    }

    private static void handleFetchResult(Context context, LinearProgressIndicator progressBar, TextView tvWait, String[] result) {
        if (result[0].startsWith("Error")) {
            Toast.makeText(context, result[0], Toast.LENGTH_SHORT).show();
            showFailedDialog(context);
            tvWait.setText("Download Failed");
            MainActivity.downloadFroze = false;
        } else {
            progressBar.setVisibility(View.VISIBLE);
            Constant.downloadFileFromURL(context, result[0], result[1], progressBar, tvWait);
        }
    }

    public static void showFailedDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_failed, null);
        builder.setView(dialogView).setCancelable(false);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.dialog_update_button).setOnClickListener(v -> {
            restartApp(context);
            dialog.dismiss();
        });
        dialog.show();
    }

    private static void restartApp(Context context) {
        // Intent to restart the activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // Clear activity stack
        context.startActivity(intent);
        // Kill the current process
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
