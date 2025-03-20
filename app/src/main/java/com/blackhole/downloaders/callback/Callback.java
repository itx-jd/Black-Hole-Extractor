package com.blackhole.downloaders.callback;

import com.blackhole.downloaders.model.DownloadItem;

public interface Callback {
    void onResult(DownloadItem item);
}
