package com.blackhole.downloaders.fetcher;

public class VideoDataFetcherFactory {
    public static VideoDataFetcher getFetcher(String platformName) {
        return new GenericVideoDataFetcher();
    }
}