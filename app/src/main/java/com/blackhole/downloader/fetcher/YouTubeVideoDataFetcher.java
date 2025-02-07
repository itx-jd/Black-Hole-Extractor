package com.blackhole.downloader.fetcher;

import android.os.AsyncTask;

import com.blackhole.downloader.R;
import com.blackhole.downloader.callback.Callback;
import com.blackhole.downloader.helper.BlackHoleApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YouTubeVideoDataFetcher implements VideoDataFetcher {
    @Override
    public void fetchVideoData(String url, Callback callback) {
        new FetchYouTubeVideoDataTask(callback).execute(url);
    }
    private static class FetchYouTubeVideoDataTask extends AsyncTask<String, Void, String[]> {
        private Callback callback;

        FetchYouTubeVideoDataTask(Callback callback) {
            this.callback = callback;
        }

        @Override
        protected String[] doInBackground(String... params) {
            String youtubeUrl = params[0];
            String apiUrl = "https://youtube-video-and-shorts-downloader1.p.rapidapi.com/api/getYTVideo?url=" + youtubeUrl;
            String apiKey = BlackHoleApp.getContext().getString(R.string.youtube_video_and_shorts_downloader);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .addHeader("x-rapidapi-key", apiKey)
                    .addHeader("x-rapidapi-host", "youtube-video-and-shorts-downloader1.p.rapidapi.com")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return parseYouTubeVideoLinkAndTitleFromResponse(responseBody);
                } else {
                    return new String[] {"Error: " + response.message(), ""};
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new String[] {"Exception: " + e.getMessage(), ""};
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            callback.onResult(result);
        }

        private String[] parseYouTubeVideoLinkAndTitleFromResponse(String responseBody) {
            String[] result = new String[2];

            try {
                JSONObject jsonObject = new JSONObject(responseBody);
                if (!jsonObject.getBoolean("error")) {
                    // Extract the URL
                    JSONArray linksArray = jsonObject.getJSONArray("links");
                    if (linksArray.length() > 1) {
                        JSONObject secondLinkObject = linksArray.getJSONObject(1);
                        result[0] = secondLinkObject.getString("link");
                    } else {
                        result[0] = "Error: No video URL found";
                    }

                    // Extract the description
                    result[1] = jsonObject.optString("description", "Error: No title found");

                } else {
                    result[0] = "Error: API returned an error";
                    result[1] = "";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                result[0] = "Error: JSON parsing failed";
                result[1] = "Error: JSON parsing failed";
            }
            return result;
        }
    }
}
