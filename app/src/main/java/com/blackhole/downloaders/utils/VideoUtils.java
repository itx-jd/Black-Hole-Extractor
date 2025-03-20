package com.blackhole.downloaders.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blackhole.downloaders.R;
import com.blackhole.downloaders.callback.Callback;
import com.blackhole.downloaders.detector.PlatformDetector;
import com.blackhole.downloaders.fetcher.VideoDataFetcher;
import com.blackhole.downloaders.fetcher.VideoDataFetcherFactory;
import com.blackhole.downloaders.model.DownloadItem;
import com.blackhole.downloaders.ui.MainActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class VideoUtils {

    public static void fetchVideoData(Context context, LinearProgressIndicator progressBar, TextView tvWait, String url) {
         // when user use send intent we use intent url
        if (DownloadUtils.isURLValid(url)) {
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
        Toast.makeText(context, "BlackHole pasted from " + platformName, Toast.LENGTH_SHORT).show();

        VideoDataFetcher fetcher = VideoDataFetcherFactory.getFetcher(platformName);
        fetcher.fetchVideoData(url, new Callback() {
            @Override
            public void onResult(DownloadItem item) {
                Log.d("VideoUtils", "Video URL: " + item.getUrl());
                Log.d("VideoUtils", "Video Name: " + item.getName());
                Log.d("VideoUtils", "Video Source: " + item.getSource());
                Log.d("VideoUtils", "Video Extension: " + item.getFile_extension());
                handleFetchResult(context, progressBar, tvWait, item,url);
            }
        });
    }

    private static void handleFetchResult(Context context, LinearProgressIndicator progressBar,
                                          TextView tvWait, DownloadItem item, String url) {
        if (item.getUrl().startsWith("Error")) {
            Log.e("VideoUtils", "Error fetching video data: " + item.getUrl());
            Toast.makeText(context, "Failed To Fetch Download Link", Toast.LENGTH_SHORT).show();
            FirebaseUtils.reportUrlToFirebase(context, url);
            DialogUtils.showFailedDialog(context);
            tvWait.setText("Download Failed");
            MainActivity.downloadFroze = false;
        } else {
            progressBar.setVisibility(View.VISIBLE);
            DownloadUtils.downloadFileFromURL(context, item, progressBar, tvWait);
        }
    }

}
