package com.blackhole.downloaders.fetcher;

import com.blackhole.downloaders.callback.Callback;

public interface VideoDataFetcher {
    void fetchVideoData(String url, Callback callback);
}
