package com.blackhole.downloaders.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.blackhole.downloaders.ui.MainActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadUtils {

    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final long UPDATE_INTERVAL_MS = 500; // Update every 500ms

    public static void downloadFileFromURL(final Context context, final String fileURL, String title,
                                           final LinearProgressIndicator progressBar, final TextView textView) {
        createNotificationChannel(context);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder notificationBuilder = createNotificationBuilder(context, title);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = downloadFile(context, fileURL, notificationManager, notificationBuilder, progressBar, textView, handler);
            handler.post(() -> postDownload(context, result, notificationManager, notificationBuilder, progressBar, textView));
        });
    }

    private static NotificationCompat.Builder createNotificationBuilder(Context context, String title) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentText("Fetching Download Link...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Download Channel",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for file download notifications");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private static String downloadFile(Context context, String fileURL, NotificationManager notificationManager,
                                       NotificationCompat.Builder builder, LinearProgressIndicator progressBar,
                                       TextView textView, Handler handler) {
        try {
            handler.post(() -> {
                textView.setText("Downloading Now");
                progressBar.setVisibility(LinearProgressIndicator.VISIBLE);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            });

            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            int totalSize = connection.getContentLength();

            if (totalSize < 10 * 1024) {
                inputStream.close();
                return null;
            }

            String filePath = saveFile(context, inputStream, totalSize, handler, textView, progressBar, notificationManager, builder);
            inputStream.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String saveFile(Context context, InputStream inputStream, int totalSize, Handler handler,
                                   TextView textView, LinearProgressIndicator progressBar, NotificationManager manager,
                                   NotificationCompat.Builder builder) throws Exception {
        String filePath;
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";

        // Track progress in a volatile variable for thread safety
        final int[] downloadedSizeHolder = {0}; // Array to allow modification in lambda
        boolean[] isDownloading = {true}; // Flag to stop the update loop when download completes

        // Periodic update Runnable
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDownloading[0]) {
                    int downloadedSize = downloadedSizeHolder[0];
                    int percentage = (downloadedSize * 100) / totalSize;
                    int cachedMb = downloadedSize / (1024 * 1024);
                    int totalMb = totalSize / (1024 * 1024);
                    updateProgress(percentage, cachedMb, totalMb, textView, progressBar, manager, builder);
                    handler.postDelayed(this, UPDATE_INTERVAL_MS); // Schedule next update
                }
            }
        };

        // Start the periodic updates
        handler.post(updateRunnable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/BlackHole++");
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 1);

            android.net.Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri == null) return null;

            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                byte[] buffer = new byte[8192]; // Increased to 8KB for efficiency
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadedSizeHolder[0] += bytesRead;
                }
            }

            contentValues.clear();
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
            resolver.update(uri, contentValues, null, null);
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    + "/BlackHole++/" + fileName;
        } else { // Android 9 and below
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "BlackHole++");
            if (!directory.exists() && !directory.mkdirs()) {
                return null;
            }

            File outputFile = new File(directory, fileName);
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8192]; // Increased to 8KB for efficiency
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadedSizeHolder[0] += bytesRead;
                }
            }

            filePath = outputFile.getAbsolutePath();
            MediaScannerConnection.scanFile(context, new String[]{filePath}, null, null);
        }

        // Stop the periodic updates when download completes
        isDownloading[0] = false;

        // Post final update to ensure 100% is shown
        int finalPercentage = (downloadedSizeHolder[0] * 100) / totalSize;
        int finalCachedMb = downloadedSizeHolder[0] / (1024 * 1024);
        int finalTotalMb = totalSize / (1024 * 1024);
        handler.post(() -> updateProgress(finalPercentage, finalCachedMb, finalTotalMb, textView, progressBar, manager, builder));

        return filePath;
    }

    private static void updateProgress(int percentage, int cachedMb, int totalMb, TextView textView,
                                       LinearProgressIndicator progressBar, NotificationManager manager,
                                       NotificationCompat.Builder builder) {
        String progressText = percentage + "% CACHED " + cachedMb + "MB OF " + totalMb + "MB";
        textView.setText(progressText);
        progressBar.setProgress(percentage);

        Notification notification = builder
                .setContentText(progressText)
                .setProgress(100, percentage, false)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
    }

    private static void postDownload(Context context, String filePath, NotificationManager manager,
                                     NotificationCompat.Builder builder, LinearProgressIndicator progressBar,
                                     TextView textView) {
        progressBar.setVisibility(LinearProgressIndicator.INVISIBLE);

        if (filePath != null) {
            Toast.makeText(context, "File saved at: " + filePath, Toast.LENGTH_SHORT).show();
            textView.setText("Download Complete");
            builder.setContentText("Download complete").setProgress(0, 0, false);
        } else {
            Toast.makeText(context, "Failed To Fetch Download Link", Toast.LENGTH_SHORT).show();
            textView.setText("Download Failed");
            builder.setContentText("Download failed").setProgress(0, 0, false);
        }

        manager.notify(NOTIFICATION_ID, builder.build());
        manager.cancel(NOTIFICATION_ID);
        MainActivity.downloadFroze = false;
    }

    public static boolean isURLValid(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}