package com.example;

import java.io.IOException;
import java.util.LinkedList;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class PrimaryController {

    // дисплей калькулятора
    @FXML
    private Label display;

    @FXML
    private Label expressionLabel;

    @FXML
    private Label historyLabel;

    // внутреннее состояние калькулятора
    private double firstOperand = 0.0;
    private String pendingOperation = "";
    private boolean startNewNumber = true;

    private final LinkedList<String> history = new LinkedList<>();

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
        updateExpression();
    }

    // обработчик нажатия операций (+, -, *, /)
    @FXML
    private void onOperator(ActionEvent event) {
        String op = ((Button) event.getSource()).getText();
        if (!display.getText().isEmpty() && !"Ошибка".equals(display.getText())) {
            firstOperand = Double.parseDouble(display.getText());
            pendingOperation = op;
            startNewNumber = true;
            updateExpression();
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
                    if (expressionLabel != null) {
                        expressionLabel.setText("");
                    }
                    return;
                }
                result = firstOperand / secondOperand;
                break;
            default:
                return;
        }

        String operationText = formatResult(firstOperand) + " " + pendingOperation + " " + formatResult(secondOperand);
        display.setText(formatResult(result));
        addToHistory(operationText + " = " + display.getText());
        pendingOperation = "";
        startNewNumber = true;

        if (expressionLabel != null) {
            expressionLabel.setText("");
        }
        updateHistoryLabel();
    }

    // обработчик "C" (сброс)
    @FXML
    private void onClear() {
        display.setText("0");
        firstOperand = 0.0;
        pendingOperation = "";
        startNewNumber = true;
        if (expressionLabel != null) {
            expressionLabel.setText("");
        }
    }

    private void updateExpression() {
        if (display == null || expressionLabel == null) {
            return;
        }
        if ("Ошибка".equals(display.getText())) {
            expressionLabel.setText("");
            return;
        }

        if (pendingOperation.isEmpty()) {
            // только ввод числа, без операции
            expressionLabel.setText(display.getText());
        } else if (startNewNumber) {
            // набрали первый операнд и операцию: "5 +"
            expressionLabel.setText(formatResult(firstOperand) + " " + pendingOperation);
        } else {
            // набираем второй операнд: "5 + 8"
            expressionLabel.setText(formatResult(firstOperand) + " " + pendingOperation + " " + display.getText());
        }
    }

    private void addToHistory(String entry) {
        history.addFirst(entry);
        while (history.size() > 3) {
            history.removeLast();
        }
    }

    private void updateHistoryLabel() {
        if (historyLabel == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String line : history) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(line);
        }
        historyLabel.setText(sb.toString());
    }

    private String formatResult(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.valueOf(value);
    }
}
