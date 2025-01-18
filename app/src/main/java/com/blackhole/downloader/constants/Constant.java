package com.blackhole.downloader.constants;

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
import android.provider.Settings;
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

public class Constant {

    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1;

    public static void downloadFileFromURL(final Context context, final String fileURL,String title, final LinearProgressIndicator progressBar, TextView textView) {

        // Create notification channel (for Android O and above)
        createNotificationChannel(context);

        // Get NotificationManager
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification builder
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentText("Fetching Download Link...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        // Execute AsyncTask to perform download operation in background
        new AsyncTask<Void, Integer, String>() {

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
                    // Establish connection to the URL
                    URL url = new URL(fileURL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    // Get the input stream from the URL
                    InputStream inputStream = urlConnection.getInputStream();

                    // Get total size of the file
                    int totalSize = urlConnection.getContentLength();

                    // Check if the file size is less than 10 KB
                    if (totalSize < 10 * 1024) { // 10 KB in bytes
                        // Close the input stream
                        inputStream.close();
                        // Return null to indicate failure
                        return null;
                    }

                    // Create the directory path
                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "BlackHole++");

                    // Check if "BlackHole" directory exists, if not, create it
                    if (!directory.exists()) {
                        if (!directory.mkdirs()) {
                            // Directory creation failed, handle the error
                            return null;
                        }
                    }

                    // Create a temporary file to save the downloaded video
                    String name = UUID.randomUUID().toString().replaceAll("-", "") + ".mp4";
                    File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "BlackHole++/" + name);
                    OutputStream outputStream = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    int downloadedSize = 0;

                    // Write data from input stream to output stream
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        downloadedSize += bytesRead;
                        // Publish progress
                        publishProgress((downloadedSize * 100) / totalSize, downloadedSize / (1024 * 1024), totalSize / (1024 * 1024));
                    }

                    // Close streams
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
                // Calculate the percentage
                int percentage = values[0];

                // Calculate the cached size
                int cachedSize = values[1];

                // Calculate the total size
                int totalSize = values[2];

                // Update the TextView with the progress
                String progressText = percentage + "% CACHED " + cachedSize + "MB OF " + totalSize + "MB";
                textView.setText(progressText);

                // Update progress bar
                progressBar.setProgress(percentage);

                // Update notification progress
                Notification notification = notificationBuilder
                        .setContentText(progressText)
                        .setProgress(100, percentage, false)
                        .build();
                notificationManager.notify(NOTIFICATION_ID, notification);
            }

            @Override
            protected void onPostExecute(String filePath) {
                super.onPostExecute(filePath);
                // Hide progress bar
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                if (filePath != null) {
                    // File saved successfully, do something with the file path
                    Toast.makeText(context, "File saved at: " + filePath, Toast.LENGTH_SHORT).show();
                    textView.setText("Download Complete");

                    // Update notification to show completion
                    Notification notification = notificationBuilder
                            .setContentText("Download complete")
                            .setProgress(0, 0, false)
                            .build();
                    notificationManager.notify(NOTIFICATION_ID, notification);

                } else {
                    // Error occurred while saving the file
                    Toast.makeText(context, "Failed To Fetch Download Link", Toast.LENGTH_SHORT).show();
                    textView.setText("Download Failed");

                    // Update notification to show failure
                    Notification notification = notificationBuilder
                            .setContentText("Download failed")
                            .setProgress(0, 0, false)
                            .build();
                    notificationManager.notify(NOTIFICATION_ID, notification);
                }

                // Cancel the ongoing notification
                notificationManager.cancel(NOTIFICATION_ID);
                MainActivity.downloadFroze = false;
            }
        }.execute();
    }

    private static void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Download Channel";
            String description = "Channel for file download notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static boolean isURlValid(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    // Method to download the latest apk for update app
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void downloadAPK(final Context context, String url, final String name) {
        // Create a Uri from the URL
        Uri uri = Uri.parse(url);

        // Initialize the DownloadManager system service
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        // Set up request with the download details
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(name);
        request.setDescription("Downloading...");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name + ".apk");

        // Enqueue the download request
        final long downloadId = downloadManager.enqueue(request);

        // BroadcastReceiver to listen for download completion
        BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Check if the download was successful
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    // Create the file object for the downloaded APK
                    File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name + ".apk");

                    if (apkFile.exists()) {

                        installApk(context,apkFile);

                    } else {
                        Toast.makeText(context, "Download failed or file not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        // Register the receiver for download complete
        context.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
    }

    // Method to open and install an APK file
    public static void installApk(Context context, File apkFile) {
        try {
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    apkFile
            );

            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Check if there is an app that can handle the intent
            if (installIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(installIntent);
            } else {
                Toast.makeText(context, "No app found to handle APK installation.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}