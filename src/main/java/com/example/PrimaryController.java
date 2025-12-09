package com.example;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

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

    @FXML
    private void onDecimalPoint() {
        if ("Ошибка".equals(display.getText())) return;
        if (startNewNumber) {
            currentInput = "0.";
            startNewNumber = false;
        } else if (!currentInput.contains(".")) {
            currentInput += ".";
        }
        updateExpression();
    }

    // обработчик нажатия операций (+, -, *, /)
    @FXML
    private void onOperator(ActionEvent event) {
        String op = ((Button) event.getSource()).getText();
        if ("Ошибка".equals(display.getText())) return;

        // нормализация минуса
        if ("−".equals(op)) op = "-";

        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        if (!tokens.isEmpty()) {
            if (isOperator(lastToken())) {
                tokens.set(tokens.size() - 1, op);
            } else {
                tokens.add(op);
            }
        } else if (!currentInput.isEmpty()) {
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

    @FXML
    private void onParen(ActionEvent event) {
        String p = ((Button) event.getSource()).getText();
        if ("Ошибка".equals(display.getText()))
            return;

        if ("(".equals(p)) {
            if (!startNewNumber) {
                tokens.add(currentInput);
                startNewNumber = true;
            }
            tokens.add("(");
        } else {
            if (!startNewNumber) {
                tokens.add(currentInput);
                startNewNumber = true;
            }
            tokens.add(")");
        }
        updateExpression();
    }

    @FXML
    private void onFunction(ActionEvent event) {
        String fn = ((Button) event.getSource()).getText();
        if ("Ошибка".equals(display.getText())) return;

        try {
            double val = Double.parseDouble(currentInput);
            Double res = applyUnary(fn, val);
            if (res == null) return;

            currentInput = formatResult(res);
            startNewNumber = false;
            updateExpression();
        } catch (NumberFormatException e) {
            errorAndReset();
        }
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
        if (tks.isEmpty()) return null;
        try {
            List<String> output = new ArrayList<>();
            Deque<String> ops = new ArrayDeque<>();
            for (String tk : tks) {
                if (isOperator(tk)) {
                    while (!ops.isEmpty() && isOperator(ops.peek()) &&
                           precedence(ops.peek()) >= precedence(tk)) {
                        output.add(ops.pop());
                    }
                    ops.push(tk);
                } else if ("(".equals(tk)) {
                    ops.push(tk);
                } else if (")".equals(tk)) {
                    while (!ops.isEmpty() && !"(".equals(ops.peek())) {
                        output.add(ops.pop());
                    }
                    if (ops.isEmpty() || !"(".equals(ops.peek())) return errorAndReset();
                    ops.pop();
                } else {
                    output.add(tk);
                }
            }
            while (!ops.isEmpty()) {
                if ("(".equals(ops.peek())) return errorAndReset();
                output.add(ops.pop());
            }

            Deque<Double> stack = new ArrayDeque<>();
            for (String tk : output) {
                if (isOperator(tk)) {
                    if (stack.size() < 2) return errorAndReset();
                    double b = stack.pop();
                    double a = stack.pop();
                    Double res = applyOperation(a, b, tk);
                    if (res == null) return null;
                    stack.push(res);
                } else {
                    stack.push(Double.parseDouble(tk));
                }
            }
            return stack.size() == 1 ? stack.pop() : errorAndReset();
        } catch (Exception e) {
            return errorAndReset();
        }
    }

    private Double errorAndReset() {
        display.setText("Ошибка");
        tokens.clear();
        pendingOperation = "";
        startNewNumber = true;
        currentInput = "0";
        return null;
    }

    private int precedence(String op) {
        if ("+".equals(op) || "-".equals(op)) {
            return 1;
        }
        return 2;
    }

    private boolean isOperator(String s) {
        return "+".equals(s) || "-".equals(s) 
            || "*".equals(s) || "×".equals(s) || "/".equals(s) || "÷".equals(s);
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

    private Double applyUnary(String fn, double v) {
        try {
            switch (fn) {
                case "sin": return Math.sin(Math.toRadians(v));
                case "cos": return Math.cos(Math.toRadians(v));
                case "tan": return Math.tan(Math.toRadians(v));
                case "cot":
                    double rad = Math.toRadians(v);
                    double tanVal = Math.tan(rad);
                    if (Math.abs(tanVal) < 1e-10) return errorAndReset();
                    return 1.0 / tanVal;
                case "ln":
                    if (v <= 0) return errorAndReset();
                    return Math.log(v);
                case "log":
                    if (v <= 0) return errorAndReset();
                    return Math.log10(v);
                case "√":
                    if (v < 0) return errorAndReset();
                    return Math.sqrt(v);
                case "n!":
                    if (v < 0 || v != Math.floor(v) || v > 20) return errorAndReset();
                    return (double) factorial((int) v);
                case "abs": return Math.abs(v);
                case "exp": return Math.exp(v);
                default: return null;
            }
        } catch (Exception e) {
            return errorAndReset();
        }
    }

    private long factorial(int n) {
        long r = 1;
        for (int i = 2; i <= n; i++) r *= i;
        return r;
    }

    private String formatResult(double value) {
        if (Math.abs(value) < 1e-10) value = 0;
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.format("%.8f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
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
