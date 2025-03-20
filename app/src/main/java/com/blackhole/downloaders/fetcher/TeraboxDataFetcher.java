package com.blackhole.downloaders.fetcher;

import android.os.AsyncTask;

import com.blackhole.downloaders.callback.Callback;
import com.blackhole.downloaders.helper.BlackHoleApp;
import com.blackhole.downloaders.model.DownloadItem;
import com.blackhole.downloaders.utils.FirebaseApiManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TeraboxDataFetcher implements VideoDataFetcher {
    @Override
    public void fetchVideoData(String url, Callback callback) {
        new FetchTeraboxDataTask(callback).execute(url);
    }

    private static class FetchTeraboxDataTask extends AsyncTask<String, Void, DownloadItem> {
        private Callback callback;
        private FirebaseApiManager firebaseApiManager = FirebaseApiManager.getInstance();

        FetchTeraboxDataTask(Callback callback) {
            this.callback = callback;
        }

        @Override
        protected DownloadItem doInBackground(String... params) {
            String teraboxUrl = params[0];
            String apiUrl = "https://terabox-downloader-direct-download-link-generator2.p.rapidapi.com/url";
            String apiKey = firebaseApiManager.getTeraboxApiKey();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(apiUrl + "?url=" + teraboxUrl)
                    .get()
                    .addHeader("x-rapidapi-key", apiKey)
                    .addHeader("x-rapidapi-host", "terabox-downloader-direct-download-link-generator2.p.rapidapi.com")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return parseTeraboxDataFromResponse(responseBody);
                } else {
                    DownloadItem item = new DownloadItem();
                    item.setName("Error: " + response.message());
                    item.setUrl("Error: Request failed");
                    return item;
                }
            } catch (IOException e) {
                e.printStackTrace();
                DownloadItem item = new DownloadItem();
                item.setName("Exception: " + e.getMessage());
                item.setUrl("Error: Network failure");
                return item;
            }
        }

        @Override
        protected void onPostExecute(DownloadItem item) {
            callback.onResult(item);
        }

        private DownloadItem parseTeraboxDataFromResponse(String responseBody) {
            DownloadItem item = new DownloadItem();

            try {
                JSONArray jsonArray = new JSONArray(responseBody);
                if (jsonArray.length() > 0) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    item.setUrl(jsonObject.getString("direct_link"));
                    item.setName(jsonObject.getString("file_name"));

                    // Extract file extension from file name
                    String fileName = jsonObject.getString("file_name");
                    String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                    item.setFile_extension(extension);

                    item.setSource("terabox");
                } else {
                    item.setUrl("Error: No download URL found");
                    item.setName("Error: No file name found");
                    item.setFile_extension("unknown");
                    item.setSource("terabox");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                item.setName("Error: JSON parsing failed");
                item.setUrl("Error: JSON parsing failed");
                item.setFile_extension("unknown");
                item.setSource("terabox");
            }
            return item;
        }
    }
}