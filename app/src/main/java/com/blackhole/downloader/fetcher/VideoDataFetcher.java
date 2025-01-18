package com.blackhole.downloader.fetcher;

import com.blackhole.downloader.callback.Callback;

public interface VideoDataFetcher {
    void fetchVideoData(String url, Callback callback);
}
