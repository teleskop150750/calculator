package com.example;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class PrimaryController {

    // дисплей калькулятора
    @FXML
    private Label display;

    // внутреннее состояние калькулятора
    private double firstOperand = 0.0;
    private String pendingOperation = "";
    private boolean startNewNumber = true;

    // обработчик меню "О программе" (переход на экран secondary.fxml)
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    // обработчик меню "Выход"
    @FXML
    private void onExit() {
        Platform.exit();
    }

    // обработчик нажатия цифр
    @FXML
    private void onDigit(ActionEvent event) {
        String value = ((Button) event.getSource()).getText();
        if (startNewNumber || "Ошибка".equals(display.getText())) {
            display.setText(value);
            startNewNumber = false;
        } else {
            display.setText(display.getText() + value);
        }
    }

    // обработчик нажатия операций (+, -, *, /)
    @FXML
    private void onOperator(ActionEvent event) {
        String op = ((Button) event.getSource()).getText();
        if (!display.getText().isEmpty() && !"Ошибка".equals(display.getText())) {
            firstOperand = Double.parseDouble(display.getText());
            pendingOperation = op;
            startNewNumber = true;
        }
    }

    // обработчик "="
    @FXML
    private void onEquals() {
        if (pendingOperation.isEmpty() || startNewNumber || "Ошибка".equals(display.getText())) {
            return;
        }

        double secondOperand = Double.parseDouble(display.getText());
        double result;

        switch (pendingOperation) {
            case "+":
                result = firstOperand + secondOperand;
                break;
            case "-":
                result = firstOperand - secondOperand;
                break;
            case "*":
            case "×":
                result = firstOperand * secondOperand;
                break;
            case "/":
            case "÷":
                if (secondOperand == 0) {
                    display.setText("Ошибка");
                    pendingOperation = "";
                    startNewNumber = true;
                    return;
                }
                result = firstOperand / secondOperand;
                break;
            default:
                return;
        }

        display.setText(formatResult(result));
        pendingOperation = "";
        startNewNumber = true;
    }

    // обработчик "C" (сброс)
    @FXML
    private void onClear() {
        display.setText("0");
        firstOperand = 0.0;
        pendingOperation = "";
        startNewNumber = true;
    }

    private String formatResult(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.valueOf(value);
    }
}
