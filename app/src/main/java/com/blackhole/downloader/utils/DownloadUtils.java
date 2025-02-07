package com.blackhole.downloader.utils;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.blackhole.downloader.ui.MainActivity;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class DownloadUtils {

    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1;

    /**
     * Initiates a file download from the specified URL and shows progress in a notification.
     *
     * @param context Application context
     * @param fileURL The URL from which to download the file
     * @param title The title of the notification
     * @param progressBar A progress bar widget to show the download progress
     * @param textView A TextView widget to display the download status
     */
    public static void downloadFileFromURL(final Context context, final String fileURL, String title,
                                           final LinearProgressIndicator progressBar, TextView textView) {
        createNotificationChannel(context);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder notificationBuilder = createNotificationBuilder(context, title);

        new DownloadAsyncTask(context, notificationManager, notificationBuilder, fileURL, progressBar, textView).execute();
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
            CharSequence name = "Download Channel";
            String description = "Channel for file download notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Async task to download a file in the background and update progress.
     */
    private static class DownloadAsyncTask extends AsyncTask<Void, Integer, String> {

        private final Context context;
        private final NotificationManager notificationManager;
        private final NotificationCompat.Builder notificationBuilder;
        private final String fileURL;
        private final LinearProgressIndicator progressBar;
        private final TextView textView;

        public DownloadAsyncTask(Context context, NotificationManager notificationManager,
                                 NotificationCompat.Builder notificationBuilder, String fileURL,
                                 LinearProgressIndicator progressBar, TextView textView) {
            this.context = context;
            this.notificationManager = notificationManager;
            this.notificationBuilder = notificationBuilder;
            this.fileURL = fileURL;
            this.progressBar = progressBar;
            this.textView = textView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textView.setText("Downloading Now");
            progressBar.setVisibility(ProgressBar.VISIBLE);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(fileURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                int totalSize = urlConnection.getContentLength();

                if (totalSize < 10 * 1024) {
                    inputStream.close();
                    return null;
                }

                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "BlackHole++");
                if (!directory.exists()) {
                    if (!directory.mkdirs()) return null;
                }

                String name = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
                File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "BlackHole++/" + name);
                OutputStream outputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];
                int bytesRead, downloadedSize = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    downloadedSize += bytesRead;
                    publishProgress((downloadedSize * 100) / totalSize, downloadedSize / (1024 * 1024), totalSize / (1024 * 1024));
                }

                outputStream.close();
                inputStream.close();

                return outputFile.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int percentage = values[0];
            int cachedSize = values[1];
            int totalSize = values[2];

            String progressText = percentage + "% CACHED " + cachedSize + "MB OF " + totalSize + "MB";
            textView.setText(progressText);
            progressBar.setProgress(percentage);

            Notification notification = notificationBuilder
                    .setContentText(progressText)
                    .setProgress(100, percentage, false)
                    .build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        @Override
        protected void onPostExecute(String filePath) {
            super.onPostExecute(filePath);
            progressBar.setVisibility(ProgressBar.INVISIBLE);

            if (filePath != null) {
                Toast.makeText(context, "File saved at: " + filePath, Toast.LENGTH_SHORT).show();
                textView.setText("Download Complete");

                Notification notification = notificationBuilder
                        .setContentText("Download complete")
                        .setProgress(0, 0, false)
                        .build();
                notificationManager.notify(NOTIFICATION_ID, notification);
            } else {
                Toast.makeText(context, "Failed To Fetch Download Link", Toast.LENGTH_SHORT).show();
                textView.setText("Download Failed");

                Notification notification = notificationBuilder
                        .setContentText("Download failed")
                        .setProgress(0, 0, false)
                        .build();
                notificationManager.notify(NOTIFICATION_ID, notification);
            }

            notificationManager.cancel(NOTIFICATION_ID);
            MainActivity.downloadFroze = false;
        }
    }

    /**
     * Verifies whether the provided URL is valid.
     *
     * @param url The URL to validate
     * @return True if valid, false otherwise
     */
    public static boolean isURLValid(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}

