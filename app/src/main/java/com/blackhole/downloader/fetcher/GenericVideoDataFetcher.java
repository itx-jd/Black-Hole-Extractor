package com.blackhole.downloader.fetcher;

import android.os.AsyncTask;

import com.blackhole.downloader.R;
import com.blackhole.downloader.callback.Callback;
import com.blackhole.downloader.helper.BlackHoleApp;

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

    private static class FetchVideoDataTask extends AsyncTask<String, Void, String[]> {
        private Callback callback;

        FetchVideoDataTask(Callback callback) {
            this.callback = callback;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String clipboardText = params[0];
            String apiUrl = "https://social-download-all-in-one.p.rapidapi.com/v1/social/autolink";
            String apiKey = BlackHoleApp.getContext().getString(R.string.social_download_all_in_one_key);

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
                    return parseVideoUrlAndTitleFromResponse(responseBody);
                } else {
                    return new String[] {"Error: " + response.message(), "Error: No title found"};
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new String[] {"Exception: " + e.getMessage(), "Error: No title found"};
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            callback.onResult(result);
        }

        private String[] parseVideoUrlAndTitleFromResponse(String responseBody) {
            String[] result = new String[2];
            try {
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray medias = jsonObject.getJSONArray("medias");

                // Extract URL
                if (medias.length() > 0) {
                    JSONObject media = medias.getJSONObject(0);
                    result[0] = media.getString("url");
                } else {
                    result[0] = "Error: No video URL found";
                }

                // Extract Title
                result[1] = jsonObject.optString("title", "Error: No title found");

            } catch (JSONException e) {
                e.printStackTrace();
                result[0] = "Error: JSON parsing failed";
                result[1] = "Error: JSON parsing failed";
            }
            return result;
        }
    }
}
