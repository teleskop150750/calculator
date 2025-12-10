package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.application.Platform;

/**
 * Контроллер основного окна калькулятора.
 * <p>
 * Обрабатывает ввод пользователя, управляет состоянием калькулятора
 * и делегирует вычисления специализированным классам.
 * </p>
 */
public class PrimaryController {

    @FXML
    private Label display;

    @FXML
    private Label historyLabel;

    /** Флаг: следующая цифра начинает новое число. */
    private boolean startNewNumber = true;
    /** История вычислений. */
    private final History history = new History();
    /** Текущие токены выражения в инфиксной записи. */
    private final List<String> tokens = new ArrayList<>();
    /** Число, которое сейчас набирает пользователь. */
    private String currentInput = "0";

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

    /**
     * Добавляет или заменяет арифметический оператор в списке токенов.
     * <p>
     * Метод обрабатывает ввод бинарных операторов (+, -, ×, ÷) с учётом
     * текущего состояния калькулятора.
     * </p>
     * 
     * <h3>Логика работы:</h3>
     * <ul>
     * <li>Если пользователь набирает число, сначала завершаем его ввод 
     *     и добавляем число в список токенов</li>
     * <li>Если предыдущий токен уже оператор – заменяем его новым 
     *     (позволяет исправить случайное нажатие)</li>
     * <li>Иначе добавляем новый оператор после текущего числа</li>
     * </ul>
     * 
     * <h3>Примеры:</h3>
     * <pre>
     * "5 +" → пользователь нажал '+' после числа 5
     * "5 + -" → пользователь передумал, нажал '−', результат: "5 -"
     * "5 + 3 ×" → после полного выражения добавляется новый оператор
     * </pre>
     * 
     * @param event событие кнопки, содержащее текст оператора
     */
    @FXML
    private void onOperator(ActionEvent event) {
        if (isErrorState())
            return;

        // Нормализуем символ оператора (например, '−' → '-')
        String op = normalizeOperator(((Button) event.getSource()).getText());

        // Если пользователь ещё набирает число, завершаем его ввод
        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        // Добавляем или заменяем оператор
        if (!tokens.isEmpty()) {
            // Если последний токен – оператор, заменяем его (исправление опечатки)
            if (isOperatorToken(lastToken())) {
                tokens.set(tokens.size() - 1, op);
            } else {
                // Иначе добавляем новый оператор
                tokens.add(op);
            }
        } else if (!currentInput.isEmpty()) {
            // Особый случай: первое число ещё не добавлено в токены
            tokens.add(currentInput);
            tokens.add(op);
            startNewNumber = true;
        }

        updateExpression();
    }

    /**
     * Добавляет оператор возведения в степень с правоассоциативностью.
     * <p>
     * Степень имеет самый высокий приоритет среди операторов и вычисляется
     * справа налево (правоассоциативна): 2^3^2 = 2^(3^2) = 2^9 = 512.
     * </p>
     * 
     * <h3>Особенности:</h3>
     * <ul>
     * <li>Приоритет выше умножения и деления</li>
     * <li>Может заменить предыдущий оператор (любой, включая другую степень)</li>
     * <li>Требует два операнда: основание и показатель</li>
     * </ul>
     * 
     * @param event событие кнопки степени
     */
    @FXML
    private void onPower(ActionEvent event) {
        if (isErrorState())
            return;

        String op = "^";

        // Завершаем ввод текущего числа, если оно набирается
        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        if (!tokens.isEmpty()) {
            // Заменяем любой предыдущий оператор (включая другую степень)
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
            double res = MathOperations.applyUnaryFunction(fn, val);

            currentInput = NumberFormatter.format(res);
            startNewNumber = false;
            updateExpression();
        } catch (Exception e) {
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

        try {
            double result = ExpressionEvaluator.evaluate(tokens);
            String exprText = String.join(" ", tokens);
            display.setText(NumberFormatter.format(result));
            history.addEntry(exprText + " = " + display.getText());

            currentInput = display.getText();
            tokens.clear();
            startNewNumber = true;
            updateHistoryLabel();
        } catch (Exception e) {
            resetToError();
        }
    }

    /** Сбрасывает калькулятор в исходное состояние. */
    @FXML
    private void onClear() {
        resetState();
    }

    // ========== Вычисления ==========

    // ========== Утилиты ==========

    /** Проверяет, является ли токен поддерживаемым арифметическим оператором. */
    private boolean isOperatorToken(String s) {
        return ExpressionEvaluator.isOperator(s);
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
    private void resetToError() {
        display.setText("Ошибка");
        tokens.clear();
        startNewNumber = true;
        currentInput = "0";
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

    /** Обновляет отображение истории, выводя старые записи сверху. */
    private void updateHistoryLabel() {
        if (historyLabel == null)
            return;

        historyLabel.setText(history.getFormattedHistory());
    }
}
