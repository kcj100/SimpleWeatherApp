package com.simpleweatherapp.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.simpleweatherapp.weather.WeatherLabels.dewPointLabel;
import static com.simpleweatherapp.weather.WeatherLabels.humidityLabel;
import static com.simpleweatherapp.weather.WeatherLabels.precipitationLabel;
import static com.simpleweatherapp.weather.WeatherLabels.temperatureLabel;
import static com.simpleweatherapp.weather.WeatherLabels.visibilityLabel;
import static com.simpleweatherapp.weather.WeatherLabels.windSpeedLabel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class WeatherAPI {

    private static final StringBuilder OPEN_METEO_API_BASE_URL = new StringBuilder("https://api.open-meteo.com/v1");

    private static String response;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static JsonNode rootNode;
    private static StringBuilder modURL;
    private double latitude;
    private double longitude;
    private int tempToggle = 0;
    private int measureToggle = 0;
    private int currentTimeIndex = 0;

    public WeatherAPI() {
        this.latitude = 0;
        this.longitude = 0;
        modURL = new StringBuilder();
    }

    public void fetchWeatherData(double lat, double lng) throws IOException, InterruptedException {
        this.latitude = lat;
        this.longitude = lng;
        HttpClient httpClient = HttpClient.newHttpClient();
        String requestUri = OPEN_METEO_API_BASE_URL.toString()
                + "/forecast?latitude=" + latitude + "&longitude=" + longitude
                + "&hourly=temperature_2m,relativehumidity_2m"
                + ",dewpoint_2m,precipitation,visibility,windspeed_10m"
                + "&models=best_match&current_weather=true&forecast_days=1"
                + "&timezone=auto";

        modURL = new StringBuilder(requestUri);
        if (tempToggle == 0) {
            setTemperatureF();
            temperatureLabel.setText(temperatureLabel.getText() + "F");
        }
        if (tempToggle == 1) {
            setTemperatureC();

        }
        if (measureToggle == 0) {
            setImperial();
        }
        if (measureToggle == 1) {
            setMetrics();
        }
        HttpRequest weatherRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(modURL.toString()))
                .timeout(Duration.ofSeconds(10)).build();

        HttpResponse weatherResponse = httpClient.send(weatherRequest,
                HttpResponse.BodyHandlers.ofString());

        response = (String) weatherResponse.body();
        rootNode = MAPPER.readTree(response);
        getCurrentTimeFromJson();
        setTemperature();
        setHumidity();
        setDewPoint();
        setPrecipitation();
        setVisibility();
        setWindSpeed();
        clearURL();
    }

    private void setTemperature(/*Label temperatureLabel*/) {
        String tempUnit = rootNode
                .get("hourly_units")
                .get("temperature_2m")
                .asText();
        double tempNum = rootNode
                .get("current_weather")
                .get("temperature")
                .asDouble();
        temperatureLabel.setText("Current Temperature: " + String.valueOf(tempNum) + tempUnit);

    }

    public void setTempToggle(int toggle) {
        this.tempToggle = toggle;
    }

    private void setTemperatureF() {
        if (!modURL.toString().contains("&temperature_unit=fahrenheit")) {
            modURL.append("&temperature_unit=fahrenheit");
        }
    }

    private void setTemperatureC() {
        for (int i = 0; i < modURL.length(); i++) {
            if (modURL.charAt(i) == '&'
                    && modURL.charAt(i + 1) == 't'
                    && modURL.charAt(i + 2) == 'e'
                    && modURL.charAt(i + 3) == 'm'
                    && modURL.charAt(i + 4) == 'p') {
                modURL.delete(i, i + 27);
            }
        }

    }

    public void setMeasureToggle(int toggle) {
        this.measureToggle = toggle;
    }

    private void setImperial() {
        if (!modURL.toString().contains("&windspeed_unit=mph")
                && !modURL.toString().contains("&windspeed_unit=mph")
                && measureToggle == 0) {
            modURL.append("&windspeed_unit=mph&precipitation_unit=inch");
        }
    }

    private void setMetrics() {
        for (int i = 0; i < modURL.length(); i++) {
            if (modURL.charAt(i) == '&'
                    && modURL.charAt(i + 1) == 'w'
                    && modURL.charAt(i + 2) == 'i'
                    && modURL.charAt(i + 3) == 'n'
                    && modURL.charAt(i + 4) == 'd') {
                modURL.delete(i, i + 18);
            }
            if (modURL.charAt(i) == '&'
                    && modURL.charAt(i + 1) == 'p'
                    && modURL.charAt(i + 2) == 'r'
                    && modURL.charAt(i + 3) == 'e'
                    && modURL.charAt(i + 4) == 'c') {
                modURL.delete(i, i + 25);
            }
        }
    }

    private void clearURL() {
        if (!this.modURL.toString().isEmpty()) {
            this.modURL.replace(0, modURL.length(), "");
        }
    }

    private void setHumidity(/*Label humidityLabel*/) {
        int humidNum = rootNode
                .get("hourly")
                .get("relativehumidity_2m")
                .get(currentTimeIndex)
                .asInt();
        humidityLabel.setText("Humidity: " + String.valueOf(humidNum)
                + rootNode.get("hourly_units").get("relativehumidity_2m").asText());
    }

    private void setDewPoint(/*Label dewPointLabel*/) {
        double dewNum = rootNode
                .get("hourly")
                .get("dewpoint_2m")
                .get(currentTimeIndex)
                .asDouble();
        dewPointLabel.setText("Dew point: " + String.valueOf(dewNum)
                + rootNode.get("hourly_units").get("dewpoint_2m").asText());
    }

    private void setPrecipitation() {
        double precipitationNum = rootNode
                .get("hourly")
                .get("precipitation")
                .get(currentTimeIndex)
                .asDouble();
        precipitationLabel.setText("Precipitation: " + String.valueOf(precipitationNum)
                + rootNode.get("hourly_units").get("precipitation").asText());
    }

    private void setVisibility() {
        double visibilityNum = rootNode
                .get("hourly")
                .get("visibility")
                .get(currentTimeIndex)
                .asDouble();
        if (rootNode.get("hourly_units").get("visibility").asText()
                .equals("ft")) {
            visibilityLabel.setText("Visibility: " + String.valueOf(
                    Math.round(convertToMiles(visibilityNum))) + "mi");
        } else if (rootNode.get("hourly_units").get("visibility").asText()
                .equals("m")) {
            visibilityLabel.setText("Visibility: " + String.valueOf(
                    Math.round(convertToKilometers(visibilityNum))) + "km");
        }

    }

    private void setWindSpeed() {
        double windSpeedNum = rootNode
                .get("hourly")
                .get("windspeed_10m")
                .get(currentTimeIndex)
                .asDouble();
        windSpeedLabel.setText("Wind speed: " + String.valueOf(windSpeedNum)
                + rootNode.get("hourly_units").get("windspeed_10m").asText());
    }

    private void getCurrentTimeFromJson() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00");
        String formattedCurrentTime = currentTime.format(formatter);
        int timeArraySize = rootNode
                .get("hourly")
                .get("time")
                .size();

        for (int i = 0; i < timeArraySize; i++) {
            if (formattedCurrentTime.equals(rootNode
                    .get("hourly").get("time").get(i).asText())) {
                this.currentTimeIndex = i;
                break;
            }
        }

    }

    private double convertToMiles(double feet) {
        return feet * 0.000189393939;
    }

    private double convertToKilometers(double meters) {
        return meters / 1000;
    }
    
}
