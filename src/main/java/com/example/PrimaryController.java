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

/**
 * Контроллер основного окна калькулятора.
 * <p>
 * Обрабатывает ввод пользователя, разбивает выражение на токены, переводит
 * инфиксную запись в обратную польскую нотацию (ОПН), вычисляет результат и
 * хранит короткую историю последних вычислений.
 * </p>
 */
public class PrimaryController {

    @FXML
    private Label display;

    @FXML
    private Label historyLabel;

    /** Флаг: следующая цифра начинает новое число. */
    private boolean startNewNumber = true;
    /** Последние вычисления, показанные в панели истории. */
    private final LinkedList<String> history = new LinkedList<>();
    /** Текущие токены выражения в инфиксной записи. */
    private final List<String> tokens = new ArrayList<>();
    /** Число, которое сейчас набирает пользователь. */
    private String currentInput = "0";
    private static final int MAX_HISTORY_SIZE = 3;
    /** Малый порог для сравнения чисел с плавающей точкой. */
    private static final double EPSILON = 1e-10;

    // ========== Навигация ==========

    /** Переходит на второе окно. */
    @FXML
    private void switchToSecondary() throws IOException {
        // Простой обработчик навигации для кнопок боковой панели.
        App.setRoot("secondary");
    }

    /** Корректно завершает приложение. */
    @FXML
    private void onExit() {
        Platform.exit();
    }

    // ========== Ввод ==========

    /** Обрабатывает цифровые кнопки и дополняет текущий токен. */
    @FXML
    private void onDigit(ActionEvent event) {
        // Добавляет цифру к числу или начинает новое.
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

    /** Вставляет десятичный разделитель, если его ещё нет. */
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

    /** Добавляет или заменяет арифметический оператор в списке токенов. */
    @FXML
    private void onOperator(ActionEvent event) {
        // Обрабатывает нажатия +, -, ×, ÷.
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

    /** Добавляет оператор возведения в степень с нужным приоритетом. */
    @FXML
    private void onPower(ActionEvent event) {
        // Отдельный обработчик для оператора степени.
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

    /** Добавляет скобки в текущее выражение. */
    @FXML
    private void onParen(ActionEvent event) {
        // Принимает как открывающую, так и закрывающую скобку.
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

    /**
     * Применяет унарные функции (тригонометрия, логарифм, факториал и т.д.) к
     * текущему числу.
     */
    @FXML
    private void onFunction(ActionEvent event) {
        // Сразу применяет выбранную функцию к текущему числу.
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

    /** Подставляет математические константы π или e в ввод. */
    @FXML
    private void onConstant(ActionEvent event) {
        // Вставляет выбранную константу как текущее число.
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

    /** Завершает выражение, вычисляет его и сохраняет в истории. */
    @FXML
    private void onEquals() {
        // Завершает выражение, вычисляет его и обновляет историю.
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

    /** Сбрасывает калькулятор в исходное состояние. */
    @FXML
    private void onClear() {
        resetState();
    }

    // ========== Вычисления ==========

    /**
     * Применяет унарную функцию (sin, log, sqrt и др.) к указанному значению.
     * Возвращает {@code null}, если функция неизвестна либо входные данные
     * некорректны.
     */
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

    /**
     * Вычисляет факториал для небольших неотрицательных чисел (ограничено для
     * защиты от переполнения).
     */
    private long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Вычисляет инфиксное выражение, представленное списком токенов.
     * Возвращает {@code null}, если вычисление не удалось.
     */
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

    /**
     * Преобразует инфиксные токены в ОПН (алгоритм сортировочной станции),
     * проверяя корректность скобок.
     */
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

    /**
     * Вычисляет выражение в ОПН.
     * Возвращает {@code null}, если стек нельзя свести к единственному значению.
     */
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

    /**
     * Выполняет бинарную арифметическую операцию.
     * Деление на ноль возвращает {@code null} и переводит UI в состояние ошибки.
     */
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

    /** Проверяет, является ли токен поддерживаемым арифметическим оператором. */
    private boolean isOperatorToken(String s) {
        return "+".equals(s) || "-".equals(s)
                || "*".equals(s) || "×".equals(s)
                || "/".equals(s) || "÷".equals(s);
    }

    /** Возвращает приоритет оператора для алгоритма сортировочной станции. */
    private int precedence(String op) {
        if ("^".equals(op))
            return 3;
        if ("+".equals(op) || "-".equals(op))
            return 1;
        return 2;
    }

    /**
     * Нормализует глифы операторов из UI (например, знак минуса) в токены парсера.
     */
    private String normalizeOperator(String op) {
        if ("−".equals(op))
            return "-";
        return op;
    }

    /** Возвращает последний токен выражения или пустую строку. */
    private String lastToken() {
        return tokens.isEmpty() ? "" : tokens.get(tokens.size() - 1);
    }

    /** Проверяет, находится ли интерфейс в состоянии ошибки. */
    private boolean isErrorState() {
        return "Ошибка".equals(display.getText());
    }

    /** Переводит калькулятор в состояние ошибки и очищает ввод. */
    private Double resetToError() {
        display.setText("Ошибка");
        tokens.clear();
        startNewNumber = true;
        currentInput = "0";
        return null;
    }

    /** Очищает выражение, состояние ввода и возвращает дисплей к нулю. */
    private void resetState() {
        display.setText("0");
        tokens.clear();
        startNewNumber = true;
        currentInput = "0";
    }

    /** Формирует строку выражения, отображаемую на основном дисплее. */
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

    /**
     * Форматирует результат, убирая лишние нули и мелкие погрешности.
     */
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

    /** Добавляет новое вычисление в ограниченную очередь истории. */
    private void addToHistory(String entry) {
        history.addFirst(entry);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }

    /** Обновляет отображение истории, выводя старые записи сверху. */
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
