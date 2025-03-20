package com.blackhole.downloaders.fetcher;

public class VideoDataFetcherFactory {
    public static VideoDataFetcher getFetcher(String platformName) {

        if(platformName.equalsIgnoreCase("Terabox")){
            return new TeraboxDataFetcher();
        }else{
            return new GenericVideoDataFetcher();
        }
    }
}