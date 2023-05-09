package com.simpleweatherapp.data;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;

public class Geocoder {

    private static final String BASE_URL = "https://api.geocodify.com/v2/geocode";
    private static String API_KEY = "";

    public static String getAPI_KEY() {
        return API_KEY;
    }

    public static void setAPI_KEY(String API_KEY) {
        Geocoder.API_KEY = API_KEY;
    }

    public String GeocodeSync(String query) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newHttpClient();

        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String requestUri = BASE_URL + "?api_key=" + API_KEY + "&q=" + encodedQuery;

        HttpRequest geocodingRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(requestUri))
                .timeout(Duration.ofSeconds(10)).build();

        HttpResponse geocodingResponse = httpClient.send(geocodingRequest,
                HttpResponse.BodyHandlers.ofString());
        return (String) geocodingResponse.body();

    }

    public int checkAPIKey(String apiKey) {
        API_KEY = apiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("Invalid API key. Please enter a valid API key");
            return 1;
        }

        // Make a test request to the Geocodify API to check the validity of the API key
        String testQuery = "Miami FL"; // Provide a test query
        try {
            String response = GeocodeSync(testQuery);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("meta")) {
                JSONObject metaObject = jsonResponse.getJSONObject("meta");
                if (metaObject.has("error_type") && metaObject.getString("error_type").equals("auth failed")) {
                    System.out.println("Invalid API key. Please enter a valid API key");
                    return 1;
                }
            }
        } catch (IOException | InterruptedException | JSONException e) {
            // Handle exceptions if the test request fails or JSON parsing fails
            System.out.println("Failed to check the API key. Please try again later.");
            return 1;
        }

        return 0;
    }

}
