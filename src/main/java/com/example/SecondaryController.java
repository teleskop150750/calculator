package com.example;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.application.Platform;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }
}