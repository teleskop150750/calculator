package com.example;

import com.example.parser.ExpressionParser;
import com.example.util.History;
import com.example.util.NumberFormatter;

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

        Button button = (Button) event.getSource();
        // Используем userData, если есть, иначе нормализуем текст
        String op = button.getUserData() != null 
            ? button.getUserData().toString() 
            : normalizeOperator(button.getText());

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
     * Добавляет функцию как токен в выражение (sin, cos, tan и т.д.).
     * Функция добавляется с открывающей скобкой для ввода аргумента.
     */
    @FXML
    private void onFunctionToken(ActionEvent event) {
        if (isErrorState())
            return;

        Button button = (Button) event.getSource();
        // Используем userData для получения имени функции
        String fn = button.getUserData() != null 
            ? button.getUserData().toString() 
            : button.getText();

        // Завершаем текущий ввод числа, если нужно
        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        // Добавляем функцию и открывающую скобку
        tokens.add(fn);
        tokens.add("(");
        
        updateExpression();
    }

    /**
     * Удаляет последний символ из текущего ввода или последний токен.
     * Работает как клавиша Backspace.
     */
    @FXML
    private void onBackspace() {
        if (isErrorState()) {
            resetState();
            return;
        }

        if (!startNewNumber && currentInput.length() > 0) {
            // Удаляем символ из текущего числа
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if (currentInput.isEmpty()) {
                currentInput = "0";
                startNewNumber = true;
            }
        } else if (!tokens.isEmpty()) {
            // Удаляем последний токен
            String lastToken = tokens.remove(tokens.size() - 1);
            
            // Если удалили число (не оператор, не скобку, не константу, не запятую), восстанавливаем для редактирования
            boolean isSpecialToken = isOperatorToken(lastToken) 
                || "(".equals(lastToken) 
                || ")".equals(lastToken)
                || ",".equals(lastToken)
                || "pi".equals(lastToken) 
                || "e".equals(lastToken);
                
            if (!isSpecialToken && lastToken.matches("\\d+(\\.\\d+)?")) {
                currentInput = lastToken;
                startNewNumber = false;
            }
        }
        
        updateExpression();
    }

    /**
     * Добавляет запятую как разделитель аргументов функции.
     * Используется для функций с несколькими аргументами (например, max).
     */
    @FXML
    private void onComma() {
        if (isErrorState())
            return;

        // Завершаем текущий ввод числа
        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        // Добавляем запятую как разделитель
        tokens.add(",");
        
        updateExpression();
    }

    /** Подставляет математические константы π или e в ввод. */
    @FXML
    private void onConstant(ActionEvent event) {
        // Добавляет константу как токен в выражение.
        if (isErrorState()) {
            resetState();
        }

        Button button = (Button) event.getSource();
        // Используем userData для получения имени константы
        String constant = button.getUserData() != null 
            ? button.getUserData().toString() 
            : button.getText();

        // Завершаем текущий ввод числа, если нужно
        if (!startNewNumber && !currentInput.isEmpty()) {
            tokens.add(currentInput);
            startNewNumber = true;
        }

        // Добавляем константу как токен
        tokens.add(constant);
        startNewNumber = true;
        
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
            String expression = String.join("", tokens);
            ExpressionParser parser = new ExpressionParser(expression);
            double result = parser.evaluate();
            
            display.setText(NumberFormatter.format(result));
            history.addEntry(expression + " = " + display.getText());

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
        return s.matches("[+\\-*/^]");
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
