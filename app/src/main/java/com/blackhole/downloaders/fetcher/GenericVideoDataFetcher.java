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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GenericVideoDataFetcher implements VideoDataFetcher {
    @Override
    public void fetchVideoData(String url, Callback callback) {
        new FetchVideoDataTask(callback).execute(url);
    }

    private static class FetchVideoDataTask extends AsyncTask<String, Void, DownloadItem> {
        private Callback callback;
        private FirebaseApiManager firebaseApiManager = FirebaseApiManager.getInstance();

        FetchVideoDataTask(Callback callback) {
            this.callback = callback;
        }

        @Override
        protected DownloadItem doInBackground(String... params) {
            String clipboardText = params[0];
            String apiUrl = "https://social-download-all-in-one.p.rapidapi.com/v1/social/autolink";
            String apiKey = firebaseApiManager.getRapidApiKey();

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"url\":\"" + clipboardText + "\"}");
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("x-rapidapi-key", apiKey)
                    .addHeader("x-rapidapi-host", "social-download-all-in-one.p.rapidapi.com")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .addHeader("Accept-Language", "en-us,en;q=0.5")
                    .addHeader("Sec-Fetch-Mode", "navigate")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return parseVideoDataFromResponse(responseBody);
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

        private DownloadItem parseVideoDataFromResponse(String responseBody) {

            DownloadItem item = new DownloadItem();

            try {
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray medias = jsonObject.getJSONArray("medias");

                // Extract data if medias array has items
                if (medias.length() > 0) {
                    JSONObject media = medias.getJSONObject(0);
                    item.setUrl(media.getString("url"));
                    item.setFile_extension(media.getString("extension"));
                } else {
                    item.setUrl("Error: No video URL found");
                    item.setFile_extension("unknown");
                }

                // Set name (title) and source
                item.setName(jsonObject.optString("title", "Error: No title found"));
                item.setSource(jsonObject.optString("source", "unknown"));

            } catch (JSONException e) {
                e.printStackTrace();
                item.setName("Error: JSON parsing failed");
                item.setUrl("Error: JSON parsing failed");
                item.setFile_extension("unknown");
                item.setSource("unknown");
            }
            return item;
        }
    }
}