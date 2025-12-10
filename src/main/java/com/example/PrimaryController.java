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
    private static final int MAX_HISTORY_SIZE = 3;
    private static final double EPSILON = 1e-10;

    // ========== Навигация ==========

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    // ========== Ввод ==========

    @FXML
    private void onDigit(ActionEvent event) {
        if (isErrorState())
            return;

        String value = ((Button) event.getSource()).getText();
        if (startNewNumber) {
            currentInput = value;
            startNewNumber = false;
        } else {
            currentInput += value;
        }
        updateExpression();
    }

    @FXML
    private void onDecimalPoint() {
        if (isErrorState())
            return;

        if (startNewNumber) {
            currentInput = "0.";
            startNewNumber = false;
        } else if (!currentInput.contains(".")) {
            currentInput += ".";
        }
        updateExpression();
    }

    @FXML
    private void onOperator(ActionEvent event) {
        if (isErrorState())
            return;

        String op = normalizeOperator(((Button) event.getSource()).getText());

        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        if (!tokens.isEmpty()) {
            if (isOperatorToken(lastToken())) {
                tokens.set(tokens.size() - 1, op);
            } else {
                tokens.add(op);
            }
        } else if (!currentInput.isEmpty()) {
            tokens.add(currentInput);
            tokens.add(op);
            startNewNumber = true;
        }

        updateExpression();
    }

    @FXML
    private void onPower(ActionEvent event) {
        if (isErrorState())
            return;

        String op = "^";

        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        if (!tokens.isEmpty()) {
            if (isOperatorToken(lastToken()) || "^".equals(lastToken())) {
                tokens.set(tokens.size() - 1, op);
            } else {
                tokens.add(op);
            }
        } else if (!currentInput.isEmpty()) {
            tokens.add(currentInput);
            tokens.add(op);
            startNewNumber = true;
        }

        updateExpression();
    }

    @FXML
    private void onParen(ActionEvent event) {
        if (isErrorState())
            return;

        String paren = ((Button) event.getSource()).getText();

        if ("(".equals(paren)) {
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
        if (isErrorState())
            return;

        String fn = ((Button) event.getSource()).getText();

        try {
            double val = Double.parseDouble(currentInput);
            Double res = applyUnaryFunction(fn, val);
            if (res == null)
                return;

            currentInput = formatResult(res);
            startNewNumber = false;
            updateExpression();
        } catch (NumberFormatException e) {
            resetToError();
        }
    }

    @FXML
    private void onConstant(ActionEvent event) {
        if (isErrorState()) {
            resetState();
        }

        String constant = ((Button) event.getSource()).getText();

        if ("π".equals(constant)) {
            currentInput = String.valueOf(Math.PI);
        } else if ("e".equals(constant)) {
            currentInput = String.valueOf(Math.E);
        }
        startNewNumber = false;
        updateExpression();
    }

    @FXML
    private void onEquals() {
        if (isErrorState())
            return;

        if (!startNewNumber) {
            tokens.add(currentInput);
        }
        if (tokens.isEmpty())
            return;
        if (isOperatorToken(lastToken())) {
            tokens.remove(tokens.size() - 1);
        }

        Double result = evaluateExpression(tokens);
        if (result == null)
            return;

        String exprText = String.join(" ", tokens);
        display.setText(formatResult(result));
        addToHistory(exprText + " = " + display.getText());

        currentInput = display.getText();
        tokens.clear();
        startNewNumber = true;
        updateHistoryLabel();
    }

    @FXML
    private void onClear() {
        resetState();
    }

    // ========== Вычисления ==========

    private Double applyUnaryFunction(String fn, double v) {
        try {
            switch (fn) {
                case "sin":
                    return Math.sin(Math.toRadians(v));
                case "cos":
                    return Math.cos(Math.toRadians(v));
                case "tan":
                    return Math.tan(Math.toRadians(v));
                case "cot":
                    double tanVal = Math.tan(Math.toRadians(v));
                    if (Math.abs(tanVal) < EPSILON)
                        return resetToError();
                    return 1.0 / tanVal;
                case "ln":
                    if (v <= 0)
                        return resetToError();
                    return Math.log(v);
                case "log":
                    if (v <= 0)
                        return resetToError();
                    return Math.log10(v);
                case "√":
                    if (v < 0)
                        return resetToError();
                    return Math.sqrt(v);
                case "n!":
                    if (v < 0 || v != Math.floor(v) || v > 20)
                        return resetToError();
                    return (double) factorial((int) v);
                case "abs":
                    return Math.abs(v);
                case "exp":
                    return Math.exp(v);
                default:
                    return null;
            }
        } catch (Exception e) {
            return resetToError();
        }
    }

    private long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private Double evaluateExpression(List<String> tks) {
        if (tks.isEmpty())
            return null;

        try {
            List<String> rpn = convertToRPN(tks);
            return evaluateRPN(rpn);
        } catch (Exception e) {
            return resetToError();
        }
    }

    private List<String> convertToRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();

        for (String token : tokens) {
            if (isOperatorToken(token) || "^".equals(token)) {
                while (!ops.isEmpty() && (isOperatorToken(ops.peek()) || "^".equals(ops.peek())) &&
                        precedence(ops.peek()) >= precedence(token)) {
                    output.add(ops.pop());
                }
                ops.push(token);
            } else if ("(".equals(token)) {
                ops.push(token);
            } else if (")".equals(token)) {
                while (!ops.isEmpty() && !"(".equals(ops.peek())) {
                    output.add(ops.pop());
                }
                if (ops.isEmpty() || !"(".equals(ops.peek())) {
                    resetToError();
                    return null;
                }
                ops.pop();
            } else {
                output.add(token);
            }
        }

        while (!ops.isEmpty()) {
            if ("(".equals(ops.peek())) {
                resetToError();
                return null;
            }
            output.add(ops.pop());
        }

        return output;
    }

    private Double evaluateRPN(List<String> rpn) {
        if (rpn == null)
            return null;

        Deque<Double> stack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isOperatorToken(token) || "^".equals(token)) {
                if (stack.size() < 2)
                    return resetToError();
                double b = stack.pop();
                double a = stack.pop();
                Double res = applyBinaryOperation(a, b, token);
                if (res == null)
                    return null;
                stack.push(res);
            } else {
                stack.push(Double.parseDouble(token));
            }
        }

        return stack.size() == 1 ? stack.pop() : resetToError();
    }

    private Double applyBinaryOperation(double left, double right, String op) {
        switch (op) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
            case "×":
                return left * right;
            case "/":
            case "÷":
                if (Math.abs(right) < EPSILON)
                    return resetToError();
                return left / right;
            case "^":
                return Math.pow(left, right);
            default:
                return null;
        }
    }

    // ========== Утилиты ==========

    private boolean isOperatorToken(String s) {
        return "+".equals(s) || "-".equals(s)
                || "*".equals(s) || "×".equals(s)
                || "/".equals(s) || "÷".equals(s);
    }

    private int precedence(String op) {
        if ("^".equals(op))
            return 3;
        if ("+".equals(op) || "-".equals(op))
            return 1;
        return 2;
    }

    private String normalizeOperator(String op) {
        if ("−".equals(op))
            return "-";
        return op;
    }

    private String lastToken() {
        return tokens.isEmpty() ? "" : tokens.get(tokens.size() - 1);
    }

    private boolean isErrorState() {
        return "Ошибка".equals(display.getText());
    }

    private Double resetToError() {
        display.setText("Ошибка");
        tokens.clear();
        startNewNumber = true;
        currentInput = "0";
        return null;
    }

    private void resetState() {
        display.setText("0");
        tokens.clear();
        startNewNumber = true;
        currentInput = "0";
    }

    private void updateExpression() {
        if (display == null)
            return;
        if (isErrorState())
            return;

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

    private String formatResult(double value) {
        if (Math.abs(value) < EPSILON)
            value = 0;
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.format("%.8f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    private void addToHistory(String entry) {
        history.addFirst(entry);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }

    private void updateHistoryLabel() {
        if (historyLabel == null)
            return;

        StringBuilder sb = new StringBuilder();
        // выводим историю в обратном порядке: от старой к новой
        for (int i = history.size() - 1; i >= 0; i--) {
            String line = history.get(i);
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(line);
        }
        historyLabel.setText(sb.toString());
    }
}
