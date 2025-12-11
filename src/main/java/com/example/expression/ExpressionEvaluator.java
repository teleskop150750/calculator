package com.example.expression;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Вычислитель математических выражений на основе RPN (обратной польской
 * нотации).
 * <p>
 * Работает с токенизированными выражениями, поддерживает:
 * </p>
 * <ul>
 * <li>Числа и константы</li>
 * <li>Переменные (через карту значений)</li>
 * <li>Операторы: +, -, *, /, ^</li>
 * <li>Функции: sin, cos, tan, ln, log, sqrt, abs и др.</li>
 * </ul>
 * 
 * <h3>Архитектура:</h3>
 * <p>
 * Этот класс аналогичен ExpressionEvaluator из JS-версии. Он принимает
 * последовательность токенов в RPN и вычисляет результат используя стек.
 * </p>
 */
public class ExpressionEvaluator {

    private final Map<String, Double> constants;
    private final Map<String, FunctionDef> functions;
    private final Map<String, OperatorDef> operators;

    /**
     * Определение функции.
     */
    public static class FunctionDef {
        public final int args;
        public final java.util.function.Function<double[], Double> evaluate;

        public FunctionDef(int args, java.util.function.Function<double[], Double> evaluate) {
            this.args = args;
            this.evaluate = evaluate;
        }
    }

    /**
     * Определение оператора.
     */
    public static class OperatorDef {
        public final int args;
        public final java.util.function.Function<double[], Double> evaluate;

        public OperatorDef(int args, java.util.function.Function<double[], Double> evaluate) {
            this.args = args;
            this.evaluate = evaluate;
        }
    }

    /**
     * Создаёт вычислитель с заданными константами, функциями и операторами.
     */
    public ExpressionEvaluator(
            Map<String, Double> constants,
            Map<String, FunctionDef> functions,
            Map<String, OperatorDef> operators) {
        this.constants = constants;
        this.functions = functions;
        this.operators = operators;
    }

    /**
     * Создаёт вычислитель с настройками по умолчанию.
     */
    public static ExpressionEvaluator createDefault() {
        return new ExpressionEvaluator(
                ExpressionDefinitions.CONSTANTS,
                ExpressionDefinitions.FUNCTIONS,
                ExpressionDefinitions.OPERATORS);
    }

    /**
     * Вычисляет выражение в RPN.
     * 
     * @param rpn       список токенов в обратной польской нотации
     * @param variables карта значений переменных (может быть пустой)
     * @return результат вычисления
     * @throws IllegalArgumentException при ошибке вычисления
     */
    public double evaluate(List<Token> rpn, Map<String, Double> variables) {
        Deque<Double> stack = new ArrayDeque<>();

        for (Token token : rpn) {
            switch (token.getType()) {
                case NUMBER:
                    evaluateNumber(token, stack);
                    break;

                case CONSTANT:
                    evaluateConstant(token, stack);
                    break;

                case VARIABLE:
                    evaluateVariable(token, stack, variables);
                    break;

                case OPERATOR:
                    evaluateOperator(token, stack);
                    break;

                case FUNCTION:
                    evaluateFunction(token, stack);
                    break;

                default:
                    throw new IllegalArgumentException(
                            String.format("Недопустимый токен '%s' в позиции %d:%d",
                                    token.getValue(), token.getStart(), token.getEnd()));
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Некорректное выражение");
        }

        return stack.pop();
    }

    private void evaluateNumber(Token token, Deque<Double> stack) {
        try {
            double value = Double.parseDouble(token.getValue());
            stack.push(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Некорректное число '%s' в позиции %d:%d",
                            token.getValue(), token.getStart(), token.getEnd()));
        }
    }

    private void evaluateConstant(Token token, Deque<Double> stack) {
        Double value = constants.get(token.getValue().toLowerCase());
        if (value == null) {
            throw new IllegalArgumentException(
                    String.format("Неизвестная константа '%s' в позиции %d:%d",
                            token.getValue(), token.getStart(), token.getEnd()));
        }
        stack.push(value);
    }

    private void evaluateVariable(Token token, Deque<Double> stack, Map<String, Double> variables) {
        Double value = variables.get(token.getValue());
        if (value == null) {
            throw new IllegalArgumentException(
                    String.format("Значение переменной '%s' не задано", token.getValue()));
        }
        stack.push(value);
    }

    private void evaluateOperator(Token token, Deque<Double> stack) {
        String op = normalizeOperator(token.getValue());
        OperatorDef operator = operators.get(op);

        if (operator == null) {
            throw new IllegalArgumentException(
                    String.format("Неизвестный оператор '%s' в позиции %d:%d",
                            token.getValue(), token.getStart(), token.getEnd()));
        }

        if (stack.size() < operator.args) {
            throw new IllegalArgumentException(
                    String.format("Недостаточно аргументов для оператора '%s' в позиции %d:%d",
                            token.getValue(), token.getStart(), token.getEnd()));
        }

        double[] args = new double[operator.args];
        for (int i = operator.args - 1; i >= 0; i--) {
            args[i] = stack.pop();
        }

        double result = operator.evaluate.apply(args);
        stack.push(result);
    }

    private void evaluateFunction(Token token, Deque<Double> stack) {
        FunctionDef func = functions.get(token.getValue().toLowerCase());

        if (func == null) {
            throw new IllegalArgumentException(
                    String.format("Неизвестная функция '%s' в позиции %d:%d",
                            token.getValue(), token.getStart(), token.getEnd()));
        }

        if (stack.size() < func.args) {
            throw new IllegalArgumentException(
                    String.format("Недостаточно аргументов для функции '%s' в позиции %d:%d",
                            token.getValue(), token.getStart(), token.getEnd()));
        }

        double[] args = new double[func.args];
        for (int i = func.args - 1; i >= 0; i--) {
            args[i] = stack.pop();
        }

        double result = func.evaluate.apply(args);
        stack.push(result);
    }

    /**
     * Нормализует символы операторов (× → *, ÷ → /).
     */
    private String normalizeOperator(String op) {
        switch (op) {
            case "×":
                return "*";
            case "÷":
                return "/";
            default:
                return op;
        }
    }

}
