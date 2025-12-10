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
     * Применяет унарную математическую функцию к указанному значению.
     * <p>
     * Поддерживаемые функции включают тригонометрические (sin, cos, tan, cot),
     * логарифмические (ln, log), алгебраические (sqrt, abs, exp) и факториал.
     * </p>
     * 
     * <h3>Обработка ошибок:</h3>
     * <ul>
     * <li><b>Тригонометрия:</b> cot(90°) и подобные углы вызывают ошибку деления на ноль</li>
     * <li><b>Логарифмы:</b> отрицательные и нулевые аргументы недопустимы</li>
     * <li><b>Корень:</b> отрицательные числа вызывают ошибку (комплексные числа не поддерживаются)</li>
     * <li><b>Факториал:</b> только неотрицательные целые числа до 20 (защита от переполнения)</li>
     * </ul>
     * 
     * <h3>Примечания:</h3>
     * <ul>
     * <li>Тригонометрические функции работают в градусах (не радианах)</li>
     * <li>ln – натуральный логарифм (по основанию e)</li>
     * <li>log – десятичный логарифм (по основанию 10)</li>
     * <li>exp – экспонента (e^x)</li>
     * </ul>
     * 
     * @param fn название функции (например, "sin", "ln", "√")
     * @param v  аргумент функции
     * @return результат вычисления или {@code null} при ошибке
     */
    private Double applyUnaryFunction(String fn, double v) {
        try {
            switch (fn) {
                case "sin":
                    // Преобразуем градусы в радианы для Math.sin
                    return Math.sin(Math.toRadians(v));
                case "cos":
                    return Math.cos(Math.toRadians(v));
                case "tan":
                    return Math.tan(Math.toRadians(v));
                case "cot":
                    // Котангенс = 1/тангенс, проверяем деление на ноль
                    double tanVal = Math.tan(Math.toRadians(v));
                    if (Math.abs(tanVal) < EPSILON)
                        return resetToError();
                    return 1.0 / tanVal;
                case "ln":
                    // Натуральный логарифм определён только для положительных чисел
                    if (v <= 0)
                        return resetToError();
                    return Math.log(v);
                case "log":
                    // Десятичный логарифм (по основанию 10)
                    if (v <= 0)
                        return resetToError();
                    return Math.log10(v);
                case "√":
                    // Квадратный корень из отрицательного числа не определён в действительных числах
                    if (v < 0)
                        return resetToError();
                    return Math.sqrt(v);
                case "n!":
                    // Факториал: только для целых неотрицательных чисел до 20
                    // Ограничение предотвращает переполнение long
                    if (v < 0 || v != Math.floor(v) || v > 20)
                        return resetToError();
                    return (double) factorial((int) v);
                case "abs":
                    // Модуль (абсолютное значение)
                    return Math.abs(v);
                case "exp":
                    // Экспонента: e^v
                    return Math.exp(v);
                default:
                    return null;
            }
        } catch (Exception e) {
            return resetToError();
        }
    }

    /**
     * Вычисляет факториал целого неотрицательного числа.
     * <p>
     * Итеративный алгоритм более эффективен, чем рекурсивный,
     * и не вызывает переполнения стека.
     * </p>
     * 
     * <h3>Ограничения:</h3>
     * <ul>
     * <li>Максимальное значение n = 20 (20! = 2,432,902,008,176,640,000)</li>
     * <li>21! уже превышает максимальное значение long (9,223,372,036,854,775,807)</li>
     * </ul>
     * 
     * @param n неотрицательное целое число от 0 до 20
     * @return n! (факториал числа n)
     */
    private long factorial(int n) {
        long result = 1;
        // 0! = 1 по определению, поэтому цикл начинается с 2
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
     * Преобразует инфиксные токены в обратную польскую нотацию (ОПН) с помощью
     * алгоритма сортировочной станции Дейкстры.
     * <p>
     * Алгоритм обрабатывает входные токены слева направо, используя стек операторов
     * для управления порядком операций в соответствии с их приоритетом и скобками.
     * </p>
     * 
     * <h3>Принцип работы:</h3>
     * <ul>
     * <li><b>Числа</b> – сразу добавляются в выходную последовательность</li>
     * <li><b>Операторы</b> – помещаются в стек с учётом приоритета:
     *     <ul>
     *     <li>Операторы с более высоким или равным приоритетом сначала извлекаются
     *         из стека в выход, затем текущий оператор помещается в стек</li>
     *     <li>Это гарантирует правильный порядок вычислений (умножение перед сложением и т.д.)</li>
     *     </ul>
     * </li>
     * <li><b>Открывающая скобка '('</b> – всегда помещается в стек операторов,
     *     создавая новый уровень вложенности</li>
     * <li><b>Закрывающая скобка ')'</b> – извлекает операторы из стека в выход
     *     до соответствующей открывающей скобки:
     *     <ul>
     *     <li>Все операторы между скобками переносятся в выходную последовательность</li>
     *     <li>Открывающая скобка удаляется из стека (но не добавляется в выход)</li>
     *     <li>Если парная скобка не найдена – возникает ошибка синтаксиса</li>
     *     </ul>
     * </li>
     * </ul>
     * 
     * <h3>Обработка ошибок:</h3>
     * <ul>
     * <li>Несбалансированные скобки (лишние открывающие или закрывающие)</li>
     * <li>Некорректная структура выражения</li>
     * </ul>
     * 
     * <h3>Пример преобразования:</h3>
     * <pre>
     * Инфикс:  3 + 4 × 2 ÷ ( 1 − 5 ) ^ 2
     * ОПН:     3 4 2 × 1 5 − 2 ^ ÷ +
     * </pre>
     * 
     * @param tokens список токенов в инфиксной записи (числа, операторы, скобки)
     * @return список токенов в обратной польской нотации, или {@code null} при ошибке
     * @see <a href="https://ru.wikipedia.org/wiki/Алгоритм_сортировочной_станции">
     *      Алгоритм сортировочной станции</a>
     */
    private List<String> convertToRPN(List<String> tokens) {
        // Выходная последовательность в ОПН
        List<String> output = new ArrayList<>();
        // Стек для временного хранения операторов и скобок
        Deque<String> ops = new ArrayDeque<>();

        // Обрабатываем каждый токен входного выражения последовательно
        for (String token : tokens) {
            // ========== Обработка операторов (+, -, ×, ÷, ^) ==========
            if (isOperatorToken(token) || "^".equals(token)) {
                // Извлекаем из стека все операторы с приоритетом >= текущего
                // Это обеспечивает правильный порядок операций
                // Например, для "3 + 4 × 2": когда встречаем '+', сначала выполнится '×'
                while (!ops.isEmpty() && (isOperatorToken(ops.peek()) || "^".equals(ops.peek())) &&
                        precedence(ops.peek()) >= precedence(token)) {
                    output.add(ops.pop());
                }
                // Помещаем текущий оператор в стек для последующей обработки
                ops.push(token);
            } 
            // ========== Обработка открывающей скобки ==========
            else if ("(".equals(token)) {
                // Открывающая скобка всегда помещается в стек
                // Она служит маркером начала подвыражения
                ops.push(token);
            } 
            // ========== Обработка закрывающей скобки ==========
            else if (")".equals(token)) {
                // Извлекаем все операторы до соответствующей открывающей скобки
                // Это завершает вычисление подвыражения в скобках
                while (!ops.isEmpty() && !"(".equals(ops.peek())) {
                    output.add(ops.pop());
                }
                // Проверка на несбалансированные скобки
                // Если стек пуст или открывающая скобка не найдена – синтаксическая ошибка
                if (ops.isEmpty() || !"(".equals(ops.peek())) {
                    resetToError();
                    return null;
                }
                // Удаляем открывающую скобку из стека (она не нужна в выходной последовательности)
                ops.pop();
            } 
            // ========== Обработка чисел ==========
            else {
                // Числа (операнды) сразу добавляются в выходную последовательность
                // В ОПН операнды идут перед операторами
                output.add(token);
            }
        }

        // ========== Завершение: извлечение оставшихся операторов ==========
        // После обработки всех токенов переносим все операторы из стека в выход
        while (!ops.isEmpty()) {
            // Если в стеке осталась открывающая скобка – значит, нет парной закрывающей
            // Это ошибка несбалансированных скобок
            if ("(".equals(ops.peek())) {
                resetToError();
                return null;
            }
            output.add(ops.pop());
        }

        return output;
    }

    /**
     * Вычисляет выражение в обратной польской нотации (ОПН).
     * <p>
     * ОПН позволяет вычислять выражения без учёта приоритета операторов
     * и скобок, используя простой алгоритм со стеком.
     * </p>
     * 
     * <h3>Алгоритм:</h3>
     * <ol>
     * <li>Проходим по токенам ОПН слева направо</li>
     * <li>Если токен – число, помещаем его в стек</li>
     * <li>Если токен – оператор:
     *     <ul>
     *     <li>Извлекаем два верхних числа из стека</li>
     *     <li>Применяем оператор к ним</li>
     *     <li>Результат помещаем обратно в стек</li>
     *     </ul>
     * </li>
     * <li>В конце в стеке должно остаться ровно одно число – результат</li>
     * </ol>
     * 
     * <h3>Пример:</h3>
     * <pre>
     * ОПН: 3 4 2 × +
     * Шаг 1: стек [3]
     * Шаг 2: стек [3, 4]
     * Шаг 3: стек [3, 4, 2]
     * Шаг 4: × → извлекаем 2 и 4, вычисляем 4×2=8, стек [3, 8]
     * Шаг 5: + → извлекаем 8 и 3, вычисляем 3+8=11, стек [11]
     * Результат: 11
     * </pre>
     * 
     * <h3>Обработка ошибок:</h3>
     * <ul>
     * <li>Недостаточно операндов для оператора</li>
     * <li>В конце вычисления в стеке больше одного числа (некорректная ОПН)</li>
     * <li>Ошибки при выполнении операций (деление на ноль и т.д.)</li>
     * </ul>
     * 
     * @param rpn список токенов в обратной польской нотации
     * @return результат вычисления или {@code null} при ошибке
     */
    private Double evaluateRPN(List<String> rpn) {
        if (rpn == null)
            return null;

        // Стек для хранения промежуточных результатов
        Deque<Double> stack = new ArrayDeque<>();

        for (String token : rpn) {
            // Проверяем, является ли токен оператором
            if (isOperatorToken(token) || "^".equals(token)) {
                // Для бинарного оператора нужны два операнда
                if (stack.size() < 2)
                    return resetToError();
                
                // Важно: порядок извлечения! Первым извлекается правый операнд
                double b = stack.pop();  // правый операнд
                double a = stack.pop();  // левый операнд
                
                // Применяем операцию (a оператор b)
                Double res = applyBinaryOperation(a, b, token);
                if (res == null)
                    return null;
                
                // Результат операции становится новым операндом
                stack.push(res);
            } else {
                // Токен – число, помещаем его в стек
                stack.push(Double.parseDouble(token));
            }
        }

        // После обработки всех токенов в стеке должно остаться ровно одно число
        // Если больше – выражение некорректно (лишние операнды)
        return stack.size() == 1 ? stack.pop() : resetToError();
    }

    /**
     * Выполняет бинарную арифметическую операцию над двумя числами.
     * <p>
     * Поддерживает основные арифметические операции: сложение, вычитание,
     * умножение, деление и возведение в степень.
     * </p>
     * 
     * <h3>Обработка особых случаев:</h3>
     * <ul>
     * <li><b>Деление на ноль:</b> проверяется с учётом погрешности вычислений
     *     (|divisor| < EPSILON), возвращает {@code null} и показывает ошибку</li>
     * <li><b>Степень:</b> использует Math.pow, поддерживает дробные показатели
     *     (например, 4^0.5 = 2)</li>
     * <li><b>Переполнение:</b> результат может быть бесконечностью (Infinity)
     *     при очень больших числах</li>
     * </ul>
     * 
     * <h3>Нормализация операторов:</h3>
     * <ul>
     * <li>'×' и '*' обрабатываются как умножение</li>
     * <li>'÷' и '/' обрабатываются как деление</li>
     * </ul>
     * 
     * @param left  левый операнд
     * @param right правый операнд
     * @param op    оператор (+, -, ×, ÷, ^)
     * @return результат операции или {@code null} при ошибке (например, деление на ноль)
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
                // Проверка деления на ноль с учётом погрешности вычислений
                if (Math.abs(right) < EPSILON)
                    return resetToError();
                return left / right;
            case "^":
                // Возведение в степень: left^right
                // Примеры: 2^3=8, 4^0.5=2, 10^(-1)=0.1
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

    /**
     * Возвращает приоритет оператора для алгоритма сортировочной станции.
     * <p>
     * Приоритеты определяют порядок выполнения операций:
     * </p>
     * <ul>
     * <li><b>Уровень 3 (наивысший):</b> возведение в степень (^)</li>
     * <li><b>Уровень 2:</b> умножение и деление (×, ÷, *, /)</li>
     * <li><b>Уровень 1:</b> сложение и вычитание (+, -)</li>
     * </ul>
     * 
     * <h3>Примеры влияния на порядок:</h3>
     * <pre>
     * 2 + 3 × 4  →  2 + (3 × 4)  = 14  (× имеет приоритет 2 > + приоритет 1)
     * 2 ^ 3 × 4  →  (2 ^ 3) × 4  = 32  (^ имеет приоритет 3 > × приоритет 2)
     * </pre>
     * 
     * @param op оператор (+, -, ×, ÷, ^)
     * @return числовой приоритет (1-3), где большее значение = выше приоритет
     */
    private int precedence(String op) {
        if ("^".equals(op))
            return 3;  // Степень – самый высокий приоритет
        if ("+".equals(op) || "-".equals(op))
            return 1;  // Сложение и вычитание – самый низкий
        return 2;      // Умножение и деление – средний уровень
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
     * Форматирует результат вычисления для удобного отображения.
     * <p>
     * Убирает незначащие нули и десятичную точку для целых чисел,
     * а также корректирует значения, близкие к нулю из-за погрешностей
     * вычислений с плавающей точкой.
     * </p>
     * 
     * <h3>Примеры форматирования:</h3>
     * <pre>
     * 5.0        →  "5"         (целое число)
     * 3.14159265 →  "3.14159265" (до 8 знаков)
     * 2.50000000 →  "2.5"       (убраны нули)
     * 0.00000001 →  "0"         (близко к нулю)
     * </pre>
     * 
     * <h3>Обработка погрешностей:</h3>
     * <ul>
     * <li>Значения |x| < 1e-10 округляются до нуля</li>
     * <li>Это предотвращает отображение вроде "2.9999999999" вместо "3"</li>
     * </ul>
     * 
     * @param value число для форматирования
     * @return отформатированная строка
     */
    private String formatResult(double value) {
        // Очень малые значения (погрешность) приводим к нулю
        if (Math.abs(value) < EPSILON)
            value = 0;
        
        // Если число целое, показываем без дробной части
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        
        // Для дробных чисел: до 8 знаков, убираем концевые нули
        return String.format("%.8f", value)
                .replaceAll("0+$", "")      // Удаляем нули в конце
                .replaceAll("\\.$", "");    // Удаляем точку, если дробной части нет
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
