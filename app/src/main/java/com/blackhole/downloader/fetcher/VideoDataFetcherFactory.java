package com.blackhole.downloader.fetcher;

public class VideoDataFetcherFactory {
    public static VideoDataFetcher getFetcher(String platformName) {
        if (platformName.equalsIgnoreCase("YouTube")) {
            return new YouTubeVideoDataFetcher();
        } else {
            return new GenericVideoDataFetcher();
        }
    }
}
