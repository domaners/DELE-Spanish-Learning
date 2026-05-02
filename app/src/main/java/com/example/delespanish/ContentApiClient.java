package com.example.delespanish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class ContentApiClient {
    private final String baseUrl;

    ContentApiClient(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    List<Article> fetchArticles() throws IOException, JSONException {
        JSONArray payload = getJsonArray("/articles");
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < payload.length(); i++) {
            JSONObject item = payload.getJSONObject(i);
            articles.add(new Article(
                    DeleLevel.valueOf(item.getString("level")),
                    item.getString("title"),
                    item.getString("grammarFocus"),
                    item.optString("summary", ""),
                    item.getString("body"),
                    readStringArray(item.getJSONArray("vocabulary"))
            ));
        }
        return articles;
    }

    List<VocabularyEntry> fetchVocabulary() throws IOException, JSONException {
        JSONArray payload = getJsonArray("/vocabulary");
        List<VocabularyEntry> entries = new ArrayList<>();
        for (int i = 0; i < payload.length(); i++) {
            JSONObject item = payload.getJSONObject(i);
            entries.add(new VocabularyEntry(
                    DeleLevel.valueOf(item.getString("level")),
                    item.getString("spanish"),
                    item.getString("english"),
                    item.getString("theme"),
                    item.getString("example")
            ));
        }
        return entries;
    }

    private JSONArray getJsonArray(String path) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
        String body = readFully(stream);
        connection.disconnect();

        if (status < 200 || status >= 300) {
            throw new IOException("Content API returned HTTP " + status + ": " + body);
        }
        return new JSONArray(body);
    }

    private List<String> readStringArray(JSONArray array) throws JSONException {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getString(i));
        }
        return values;
    }

    private String readFully(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
