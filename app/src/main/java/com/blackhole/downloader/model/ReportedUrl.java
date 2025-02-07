package com.blackhole.downloader.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportedUrl {
    private String url;
    private String timestamp;
    private String dateTime;
    private String deviceModel;
    private String androidVersion;
    private String appVersion;
    private String networkType;
    private String batteryPercentage;
    private String timeZone;

    // Constructor
    public ReportedUrl(String url, long timestamp, String deviceModel, String androidVersion,
                       String appVersion, String networkType, int batteryPercentage, String timeZone) {
        this.url = url;
        this.timestamp = String.valueOf(timestamp);
        this.dateTime = formatDate(timestamp);
        this.deviceModel = deviceModel;
        this.androidVersion = androidVersion;
        this.appVersion = appVersion;
        this.networkType = networkType;
        this.batteryPercentage = batteryPercentage + "%";
        this.timeZone = timeZone;
    }

    // Method to format timestamp to a readable date-time
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Getters (Required for Firebase)
    public String getUrl() { return url; }
    public String getTimestamp() { return timestamp; }
    public String getDateTime() { return dateTime; }
    public String getDeviceModel() { return deviceModel; }
    public String getAndroidVersion() { return androidVersion; }
    public String getAppVersion() { return appVersion; }
    public String getNetworkType() { return networkType; }
    public String getBatteryPercentage() { return batteryPercentage; }
    public String getTimeZone() { return timeZone; }
}
