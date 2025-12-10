package com.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Класс для вычисления математических выражений.
 * <p>
 * Использует алгоритм сортировочной станции Дейкстры для преобразования
 * инфиксной записи в обратную польскую нотацию (ОПН), а затем вычисляет
 * результат выражения.
 * </p>
 */
public class ExpressionEvaluator {

    /**
     * Вычисляет инфиксное выражение, представленное списком токенов.
     * 
     * @param tokens список токенов в инфиксной записи
     * @return результат вычисления
     * @throws IllegalArgumentException при ошибке вычисления
     */
    public static double evaluate(List<String> tokens) {
        if (tokens.isEmpty())
            throw new IllegalArgumentException("Пустое выражение");

        List<String> rpn = convertToRPN(tokens);
        return evaluateRPN(rpn);
    }

    /**
     * Проверяет, является ли токен поддерживаемым арифметическим оператором.
     */
    public static boolean isOperator(String s) {
        return "+".equals(s) || "-".equals(s)
                || "*".equals(s) || "×".equals(s)
                || "/".equals(s) || "÷".equals(s);
    }

    /**
     * Возвращает приоритет оператора для алгоритма сортировочной станции.
     * 
     * @param op оператор (+, -, ×, ÷, ^)
     * @return числовой приоритет (1-3), где большее значение = выше приоритет
     */
    public static int precedence(String op) {
        if ("^".equals(op))
            return 3;
        if ("+".equals(op) || "-".equals(op))
            return 1;
        return 2;
    }

    /**
     * Преобразует инфиксные токены в обратную польскую нотацию (ОПН) с помощью
     * алгоритма сортировочной станции Дейкстры.
     * 
     * @param tokens список токенов в инфиксной записи
     * @return список токенов в обратной польской нотации
     * @throws IllegalArgumentException при ошибке синтаксиса
     */
    private static List<String> convertToRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();

        for (String token : tokens) {
            if (isOperator(token) || "^".equals(token)) {
                while (!ops.isEmpty() && (isOperator(ops.peek()) || "^".equals(ops.peek())) &&
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
                    throw new IllegalArgumentException("Несбалансированные скобки");
                }
                ops.pop();
            } else {
                output.add(token);
            }
        }

        while (!ops.isEmpty()) {
            if ("(".equals(ops.peek())) {
                throw new IllegalArgumentException("Несбалансированные скобки");
            }
            output.add(ops.pop());
        }

        return output;
    }

    /**
     * Вычисляет выражение в обратной польской нотации (ОПН).
     * 
     * @param rpn список токенов в обратной польской нотации
     * @return результат вычисления
     * @throws IllegalArgumentException при ошибке вычисления
     */
    private static double evaluateRPN(List<String> rpn) {
        Deque<Double> stack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isOperator(token) || "^".equals(token)) {
                if (stack.size() < 2)
                    throw new IllegalArgumentException("Недостаточно операндов для оператора " + token);

                double b = stack.pop();
                double a = stack.pop();
                double result = MathOperations.applyBinaryOperation(a, b, token);
                stack.push(result);
            } else {
                try {
                    stack.push(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Некорректное число: " + token);
                }
            }
        }

        if (stack.size() != 1)
            throw new IllegalArgumentException("Некорректное выражение");

        return stack.pop();
    }
}
