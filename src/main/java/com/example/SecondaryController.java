package com.example;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.application.Platform;

/**
 * Контроллер второго окна с кнопками навигации.
 */
public class SecondaryController {

    /** Возвращает пользователя к основному окну калькулятора. */
    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }

    /** Корректно завершает приложение. */
    @FXML
    private void onExit() {
        Platform.exit();
    }
}