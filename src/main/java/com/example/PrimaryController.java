package com.example;

import com.example.parser.ExpressionParser;
import com.example.util.CurrentInput;
import com.example.util.History;
import com.example.util.NumberFormatter;
import com.example.util.TokenManager;

import java.io.IOException;

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
    private static final String ERROR_TEXT = "Ошибка";

    @FXML
    private Label display;

    @FXML
    private Label historyLabel;

    private final History history = new History();
    private final TokenManager tokenManager = new TokenManager();
    private final CurrentInput currentInput = new CurrentInput(tokenManager);

    // ========== Навигация ==========

    /**
     * Переключает интерфейс на вторичное окно.
     * 
     * @throws IOException если не удалось загрузить FXML
     */
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    /**
     * Корректно завершает работу приложения.
     */
    @FXML
    private void onExit() {
        Platform.exit();
    }

    // ========== Ввод ==========

    /**
     * Обрабатывает нажатие кнопки с цифрой.
     * <p>
     * Если начинается новое число, заменяет текущее значение,
     * иначе добавляет цифру к существующему.
     * </p>
     * 
     * @param event событие нажатия кнопки
     */
    @FXML
    private void onDigit(ActionEvent event) {
        if (isErrorState())
            return;

        String value = ((Button) event.getSource()).getText();

        currentInput.onDigit(value);

        updateExpression();
    }

    /**
     * Обрабатывает ввод десятичной точки.
     * <p>
     * Если начинается новое число, создаёт "0.",
     * иначе добавляет точку к текущему вводу.
     * </p>
     */
    @FXML
    private void onDecimalPoint() {
        if (isErrorState())
            return;

        currentInput.onDecimalPoint();

        updateExpression();
    }

    /**
     * Обрабатывает нажатие кнопки оператора (+, -, *, /, ^).
     * <p>
     * Обрабатывает специальный случай унарного минуса и
     * заменяет последний оператор, если он уже был введён.
     * Игнорирует операторы в начале выражения, кроме унарного минуса.
     * </p>
     * 
     * @param event событие нажатия кнопки
     */
    @FXML
    private void onOperator(ActionEvent event) {
        if (isErrorState())
            return;

        Button button = (Button) event.getSource();
        String operator = button.getUserData().toString();

        // if (isUnaryMinus(operator)) {
        // handleUnaryMinus();
        // return;
        // }

        finalizeCurrentNumber();

        // Если выражение пустое и ввод пустой, игнорируем оператор (кроме минуса)
        if (tokenManager.isEmpty() && currentInput.isEmpty()) {
            return; // Нельзя начинать с бинарного оператора
        }

        if (!tokenManager.isEmpty()) {
            tokenManager.replaceOrAddOperator(operator);
        } else if (!currentInput.isEmpty()) {
            tokenManager.add(currentInput.getValue());
            tokenManager.add(operator);
            tokenManager.setStartNewNumber(true);
        }

        updateExpression();
    }

    /**
     * Обрабатывает ввод открывающей или закрывающей скобки.
     * <p>
     * Перед добавлением скобки завершает текущий ввод числа.
     * Предотвращает добавление дублирующей открывающей скобки после функции.
     * </p>
     * 
     * @param event событие нажатия кнопки
     */
    @FXML
    private void onParen(ActionEvent event) {
        if (isErrorState())
            return;

        String paren = ((Button) event.getSource()).getText();

        finalizeCurrentNumber();

        // Предотвращаем двойные открывающие скобки после функций
        if ("(".equals(paren) && "(".equals(tokenManager.getLast())) {
            return; // Игнорируем дублирующую скобку
        }

        tokenManager.add(paren);

        updateExpression();
    }

    /**
     * Обрабатывает ввод функции (sin, cos, sqrt и т.д.).
     * <p>
     * Автоматически добавляет открывающую скобку после имени функции.
     * </p>
     * 
     * @param event событие нажатия кнопки
     */
    @FXML
    private void onFunctionToken(ActionEvent event) {
        if (isErrorState())
            return;

        Button button = (Button) event.getSource();
        String function = button.getUserData().toString();

        finalizeCurrentNumber();

        tokenManager.add(function);
        tokenManager.add("(");

        updateExpression();
    }

    /**
     * Обрабатывает удаление последнего символа или токена.
     * <p>
     * Если калькулятор в состоянии ошибки, сбрасывает его.
     * Если вводится число, удаляет последнюю цифру.
     * Иначе удаляет последний токен из выражения.
     * </p>
     */
    @FXML
    private void onBackspace() {
        if (isErrorState()) {
            resetState();
            return;
        }

        if (!tokenManager.isStartNewNumber() && !currentInput.isEmpty()) {
            currentInput.backspace();
            if ("0".equals(currentInput.getValue())) {
                tokenManager.setStartNewNumber(true);
            }
        } else if (!tokenManager.isEmpty()) {
            handleTokenBackspace();
        }

        updateExpression();
    }

    /**
     * Обрабатывает ввод разделителя аргументов (запятой) для функций.
     */
    @FXML
    private void onComma() {
        if (isErrorState())
            return;

        finalizeCurrentNumber();
        tokenManager.add(",");

        updateExpression();
    }

    /**
     * Обрабатывает ввод математической константы (π, e).
     * <p>
     * Если калькулятор в состоянии ошибки, сбрасывает его.
     * Получает значение константы из userData кнопки или её текста.
     * </p>
     * 
     * @param event событие нажатия кнопки
     */
    @FXML
    private void onConstant(ActionEvent event) {
        if (isErrorState()) {
            resetState();
        }

        Button button = (Button) event.getSource();
        String constant = button.getUserData() != null
                ? button.getUserData().toString()
                : button.getText();

        finalizeCurrentNumber();
        tokenManager.add(constant);
        tokenManager.setStartNewNumber(true);

        updateExpression();
    }

    /**
     * Вычисляет текущее выражение и показывает результат.
     * <p>
     * Завершает текущий ввод, удаляет оператор в конце (если есть),
     * парсит выражение и вычисляет результат. Результат добавляется
     * в историю и отображается на экране.
     * </p>
     */
    @FXML
    private void onEquals() {
        if (isErrorState())
            return;

        finalizeCurrentNumber();

        if (tokenManager.isEmpty())
            return;

        removeTrailingOperator();

        try {
            String expression = tokenManager.toExpression();
            double result = new ExpressionParser(expression).evaluate();
            String formattedResult = NumberFormatter.format(result);

            display.setText(formattedResult);
            history.addEntry(expression + " = " + formattedResult);

            currentInput.setValue(formattedResult);
            tokenManager.clear();
            tokenManager.setStartNewNumber(true);
            updateHistoryLabel();
        } catch (Exception e) {
            resetToError();
        }
    }

    /**
     * Очищает дисплей и сбрасывает состояние калькулятора.
     */
    @FXML
    private void onClear() {
        resetState();
    }

    // ========== Вспомогательные методы ==========

    /**
     * Проверяет, является ли оператор унарным минусом.
     * <p>
     * Унарный минус используется в начале числа или после другого оператора.
     * </p>
     * 
     * @param operator проверяемый оператор
     * @return true, если это унарный минус
     */
    // private boolean isUnaryMinus(String operator) {
    //     return "-".equals(operator) && tokenManager.isStartNewNumber() && tokenManager.canBeUnaryMinus();
    // }

    /**
     * Обрабатывает унарный минус, начиная новое отрицательное число.
     */
    // private void handleUnaryMinus() {
    //     currentInput.setValue("-");
    //     tokenManager.setStartNewNumber(false);
    //     updateExpression();
    // }

    /**
     * Завершает ввод текущего числа, добавляя его в список токенов.
     * <p>
     * Вызывается перед добавлением операторов, функций или скобок.
     * </p>
     */
    private void finalizeCurrentNumber() {
        if (!tokenManager.isStartNewNumber() && !currentInput.isEmpty()) {
            tokenManager.add(currentInput.getValue());
            tokenManager.setStartNewNumber(true);
        }
    }

    /**
     * Обрабатывает удаление последнего токена при нажатии backspace.
     * <p>
     * Если удалённый токен — число, восстанавливает его для редактирования.
     * </p>
     */
    private void handleTokenBackspace() {
        String lastToken = tokenManager.getLastRemoved();

        if (!tokenManager.isSpecialToken(lastToken) && tokenManager.isNumber(lastToken)) {
            currentInput.setValue(lastToken);
            tokenManager.setStartNewNumber(false);
        }
    }

    /**
     * Удаляет оператор в конце выражения перед вычислением.
     */
    private void removeTrailingOperator() {
        if (tokenManager.isOperator(tokenManager.getLast())) {
            tokenManager.removeLast();
        }
    }

    /**
     * Проверяет, находится ли калькулятор в состоянии ошибки.
     * 
     * @return true, если отображается текст ошибки
     */
    private boolean isErrorState() {
        return ERROR_TEXT.equals(display.getText());
    }

    /**
     * Переводит калькулятор в состояние ошибки.
     */
    private void resetToError() {
        display.setText(ERROR_TEXT);
        tokenManager.clear();
        tokenManager.setStartNewNumber(true);
        currentInput.clear();
    }

    /**
     * Полностью сбрасывает состояние калькулятора.
     */
    private void resetState() {
        display.setText("");
        tokenManager.clear();
        tokenManager.setStartNewNumber(true);
        currentInput.clear();
    }

    /**
     * Обновляет отображение текущего выражения на экране.
     * <p>
     * Формирует строку из токенов и текущего ввода, разделяя их пробелами.
     * </p>
     */
    private void updateExpression() {
        if (display == null || isErrorState())
            return;

        StringBuilder expression = new StringBuilder(tokenManager.toDisplayString());

        if (!tokenManager.isStartNewNumber() || tokenManager.isEmpty()) {
            if (expression.length() > 0) {
                expression.append(" ");
            }
            expression.append(currentInput.getValue());
        }

        display.setText(expression.length() == 0 ? currentInput.getValue() : expression.toString());
    }

    /**
     * Обновляет метку с историей вычислений.
     * <p>
     * Отображает последние несколько вычислений на экране истории.
     * </p>
     */
    private void updateHistoryLabel() {
        if (historyLabel != null) {
            historyLabel.setText(history.getFormattedHistory());
        }
    }
}
