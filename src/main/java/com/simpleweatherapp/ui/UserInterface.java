package com.simpleweatherapp.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpleweatherapp.data.Geocoder;
import com.simpleweatherapp.data.WeatherAPI;
import static com.simpleweatherapp.weather.WeatherLabels.dewPointLabel;
import static com.simpleweatherapp.weather.WeatherLabels.humidityLabel;
import static com.simpleweatherapp.weather.WeatherLabels.precipitationLabel;
import static com.simpleweatherapp.weather.WeatherLabels.temperatureLabel;
import static com.simpleweatherapp.weather.WeatherLabels.visibilityLabel;
import static com.simpleweatherapp.weather.WeatherLabels.windSpeedLabel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class UserInterface extends Application {

    private static int WIDTH = 800;
    private static int HEIGHT = 400;
    private double lat = 0.0;
    private double lng = 0.0;
    WeatherAPI weatherAPI = new WeatherAPI();
    private int counter = 1;
    Geocoder geocoder = new Geocoder();

    public void start(Stage window) throws IOException, InterruptedException {
        BorderPane apiMenu = new BorderPane();
        apiMenu.setPrefSize(WIDTH / 2, HEIGHT / 2);
        Label instructions = new Label("Enter Geocodify API key");
        instructions.setFont(new Font("Monospaced", 20));
        TextField apiField = new TextField();
        Button apiEnter = new Button("Enter");
        apiEnter.setFont(new Font("Monospaced", 15));
        VBox apiBox = new VBox(instructions, apiField, apiEnter);
        apiBox.setAlignment(Pos.CENTER);
        apiMenu.setCenter(apiBox);
        Scene scene = new Scene(apiMenu);
        window.setScene(scene);
        window.show();
        apiField.setOnMouseClicked((event) -> {
            if (apiField.getStyle().contains("-fx-text-fill: red;")) {
                apiField.setStyle("-fx-text-fill: black;");
                apiField.setText("");
            }
        });
        apiField.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                apiEnter.fire();
                event.consume();
            }
        });
        apiEnter.setOnAction((event) -> {
            String apiKey = apiField.getText();
            if (geocoder.checkAPIKey(apiKey) == 1) {
                apiField.setStyle("-fx-text-fill: red;");
                apiField.setText("Invalid API key");
            } else {
                System.out.println("API key valid.");
                try {
                    BorderPane layout = new BorderPane();
                    layout.setPadding(new Insets(10, 10, 10, 10));
                    layout.setPrefSize(WIDTH, HEIGHT);
                    Label city = new Label("City");
                    city.setFont(new Font("Monospaced", 50));

                    TextField searchBar = new TextField();
                    searchBar.setText("19802");
                    searchBar.setFont(new Font("Monospaced", 15));
                    Button enterButton = new Button("Enter");
                    enterButton.setFont(new Font("Monospaced", 15));

                    Button tempButton = new Button("C/F");
                    Button imperialButton = new Button("Imperial");
                    Button metricsButton = new Button("Metrics");
                    HBox buttonBox = new HBox(tempButton, imperialButton, metricsButton);
                    buttonBox.setPadding(new Insets(10, 10, 10, 10));
                    temperatureLabel.setFont(new Font("Monospaced", 35));
                    VBox cityPane = new VBox(city, temperatureLabel);
                    VBox weatherInfo = new VBox(humidityLabel, dewPointLabel,
                            precipitationLabel, visibilityLabel, windSpeedLabel);
                    weatherInfo.setAlignment(Pos.CENTER);
                    cityPane.setAlignment(Pos.TOP_CENTER);

                    weatherInfo.getChildren().stream()
                            .forEach((Node node) -> {
                                if (node instanceof Label) {
                                    Label label = (Label) node;
                                    label.setFont(new Font("Monospaced", 20));
                                }
                            });
                    HBox searchBox = new HBox(searchBar, enterButton);
                    BorderPane buttonPane = new BorderPane();
                    buttonPane.setLeft(buttonBox);
                    buttonPane.setRight(searchBox);
                    Label loading = new Label("Loading...");
                    loading.setVisible(false);
                    searchBox.getChildren().add(loading);
                    searchBox.setPadding(new Insets(10, 10, 10, 10));
                    layout.setTop(buttonPane);
                    layout.setBottom(weatherInfo);
                    layout.setCenter(cityPane);
                    geoLocation(searchBar.getText());
                    getCity(searchBar.getText(), city);
                    weatherAPI.fetchWeatherData(lat, lng);
                    addButtonListeners(tempButton, imperialButton, metricsButton);
                    addSearchBarEnterButtonListener(searchBar, enterButton, city, loading);
                    Scene view = new Scene(layout);
                    window.setScene(view);
                    window.show();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ex) {
                    Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

    }

    private void geoLocation(String query) throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();
        String response = geocoder.GeocodeSync(query);
        JsonNode rootNode = mapper.readTree(response);
        if (rootNode == null) {
            System.out.println("Error: response could not be parsed");
            return;
        }
        JsonNode responseNode = rootNode.get("response");
        if (responseNode == null) {
            System.out.println("Error: response node not found in response");
            return;
        }
        JsonNode featuresNode = responseNode.get("features");
        if (featuresNode == null || featuresNode.size() == 0) {
            System.out.println("Error: features not found in response");
            return;
        }
        lat = featuresNode.get(0)
                .get("geometry")
                .get("coordinates")
                .get(1).asDouble();
        lng = featuresNode.get(0)
                .get("geometry")
                .get("coordinates")
                .get(0).asDouble();
    }

    private void getCity(String query, Label cityLabel) throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();
        String response = geocoder.GeocodeSync(query);
        JsonNode rootNode = mapper.readTree(response);
        if (rootNode == null) {
            System.out.println("Error: response could not be parsed");
            return;
        }
        JsonNode responseNode = rootNode.get("response");
        if (responseNode == null) {
            System.out.println("Error: response node not found in response");
            return;
        }
        JsonNode featuresNode = responseNode.get("features");
        if (featuresNode == null || featuresNode.size() == 0) {
            System.out.println("Error: features not found in response");
            return;
        }
        String city = featuresNode.get(0)
                .get("properties")
                .get("label").asText();
        cityLabel.setText(city);

    }

    private void addButtonListeners(Button tempButton, Button imperialButton, Button metricsButton) {
        tempButton.setOnAction((event) -> {

            if (counter == 0) {
                try {
                    weatherAPI.setTempToggle(0);
                    weatherAPI.fetchWeatherData(lat, lng);
                    counter++;

                } catch (IOException ex) {
                    Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                try {
                    weatherAPI.setTempToggle(1);
                    weatherAPI.fetchWeatherData(lat, lng);
                    counter = 0;
                } catch (IOException ex) {
                    Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        imperialButton.setOnAction((event) -> {
            weatherAPI.setMeasureToggle(0);
            try {
                weatherAPI.fetchWeatherData(lat, lng);
            } catch (IOException ex) {
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        metricsButton.setOnAction((event) -> {
            weatherAPI.setMeasureToggle(1);
            try {
                weatherAPI.fetchWeatherData(lat, lng);
            } catch (IOException ex) {
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void addSearchBarEnterButtonListener(TextField searchBar, Button enterButton, Label cityLabel, Label loading) {
        searchBar.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                enterButton.fire();
                event.consume();
            }
        });

        enterButton.setOnAction((event) -> {
            try {
                loading.setVisible(true);
                geoLocation(searchBar.getText());
                getCity(searchBar.getText(), cityLabel);
                weatherAPI.fetchWeatherData(lat, lng);
            } catch (IOException ex) {
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                loading.setVisible(false);
            }
        });
    }

}
