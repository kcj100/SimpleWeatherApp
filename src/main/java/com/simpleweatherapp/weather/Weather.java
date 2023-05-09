package com.simpleweatherapp.weather;

import com.simpleweatherapp.ui.UserInterface;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Weather {
    
    public static void main(String[] args) throws IOException {
        Platform.startup(() -> {
        UserInterface ui = new UserInterface();
        Stage window = new Stage();
            try {
                ui.start(window);
            } catch (IOException ex) {
                Logger.getLogger(Weather.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Weather.class.getName()).log(Level.SEVERE, null, ex);
            }
    });
    }

    
}
