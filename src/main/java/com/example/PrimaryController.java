package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class PrimaryController {

    @FXML
    private Label display;

    @FXML
    private Label historyLabel;

    private boolean startNewNumber = true;
    private final LinkedList<String> history = new LinkedList<>();
    private final List<String> tokens = new ArrayList<>();
    private String currentInput = "0";
    private String pendingOperation = "";

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
            currentInput = value;
            startNewNumber = false;
        } else {
            currentInput += value;
        }
        updateExpression();
    }

    // обработчик нажатия операций (+, -, *, /)
    @FXML
    private void onOperator(ActionEvent event) {
        String op = ((Button) event.getSource()).getText();
        if ("Ошибка".equals(display.getText()))
            return;

        if (!startNewNumber) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        if (!tokens.isEmpty()) {
            if (isOperator(lastToken())) {
                tokens.set(tokens.size() - 1, op);
            } else {
                tokens.add(op);
            }
        } else {
            tokens.add(currentInput);
            tokens.add(op);
            startNewNumber = true;
        }

        pendingOperation = op;
        updateExpression();
    }

    // обработчик "="
    @FXML
    private void onEquals() {
        if ("Ошибка".equals(display.getText()))
            return;
        if (!startNewNumber)
            tokens.add(currentInput);
        if (tokens.isEmpty())
            return;
        if (isOperator(lastToken()))
            tokens.remove(tokens.size() - 1);

        Double result = evaluateTokens(tokens);
        if (result == null)
            return;

        String exprText = String.join(" ", tokens);
        display.setText(formatResult(result));
        addToHistory(exprText + " = " + display.getText());

        currentInput = display.getText();
        tokens.clear();
        pendingOperation = "";
        startNewNumber = true;
        updateHistoryLabel();
    }

    // обработчик "C" (сброс)
    @FXML
    private void onClear() {
        display.setText("0");
        tokens.clear();
        pendingOperation = "";
        startNewNumber = true;
        currentInput = "0";
    }

    private void updateExpression() {
        if (display == null)
            return;
        if ("Ошибка".equals(display.getText())) {
            display.setText("Ошибка");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            if (i > 0)
                sb.append(" ");
            sb.append(tokens.get(i));
        }
        if (!startNewNumber || tokens.isEmpty()) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(currentInput);
        }
        display.setText(sb.length() == 0 ? currentInput : sb.toString());
    }

    private Double evaluateTokens(List<String> tks) {
        if (tks.isEmpty())
            return null;
        try {
            double acc = Double.parseDouble(tks.get(0));
            for (int i = 1; i < tks.size(); i += 2) {
                String op = tks.get(i);
                double right = Double.parseDouble(tks.get(i + 1));
                Double res = applyOperation(acc, right, op);
                if (res == null)
                    return null;
                acc = res;
            }
            return acc;
        } catch (Exception ex) {
            display.setText("Ошибка");
            tokens.clear();
            pendingOperation = "";
            startNewNumber = true;
            currentInput = "0";
            return null;
        }
    }

    private boolean isOperator(String s) {
        return "+".equals(s) || "-".equals(s) || "*".equals(s) || "×".equals(s) || "/".equals(s) || "÷".equals(s);
    }

    private String lastToken() {
        return tokens.isEmpty() ? "" : tokens.get(tokens.size() - 1);
    }

    private Double applyOperation(double left, double right, String op) {
        double result;
        switch (op) {
            case "+":
                result = left + right;
                break;
            case "-":
                result = left - right;
                break;
            case "*":
            case "×":
                result = left * right;
                break;
            case "/":
            case "÷":
                if (right == 0) {
                    display.setText("Ошибка");
                    tokens.clear();
                    pendingOperation = "";
                    startNewNumber = true;
                    currentInput = "0";
                    return null;
                }
                result = left / right;
                break;
            default:
                return null;
        }
        return result;
    }

    private String formatResult(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.valueOf(value);
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
}
