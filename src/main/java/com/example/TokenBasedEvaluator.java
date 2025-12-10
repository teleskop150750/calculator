package com.example;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Вычислитель математических выражений на основе RPN (обратной польской нотации).
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
public class TokenBasedEvaluator {
    
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
    public TokenBasedEvaluator(
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
    public static TokenBasedEvaluator createDefault() {
        Map<String, Double> constants = initConstants();
        Map<String, FunctionDef> functions = initFunctions();
        Map<String, OperatorDef> operators = initOperators();
        
        return new TokenBasedEvaluator(constants, functions, operators);
    }
    
    /**
     * Вычисляет выражение в RPN.
     * 
     * @param rpn список токенов в обратной польской нотации
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
            case "×": return "*";
            case "÷": return "/";
            default: return op;
        }
    }
    
    /**
     * Инициализирует константы по умолчанию.
     */
    private static Map<String, Double> initConstants() {
        Map<String, Double> constants = new HashMap<>();
        constants.put("pi", Math.PI);
        constants.put("π", Math.PI);
        constants.put("e", Math.E);
        return constants;
    }
    
    /**
     * Инициализирует функции по умолчанию.
     */
    private static Map<String, FunctionDef> initFunctions() {
        Map<String, FunctionDef> functions = new HashMap<>();
        
        // Тригонометрические функции (в радианах)
        functions.put("sin", new FunctionDef(1, args -> Math.sin(args[0])));
        functions.put("cos", new FunctionDef(1, args -> Math.cos(args[0])));
        functions.put("tan", new FunctionDef(1, args -> Math.tan(args[0])));
        functions.put("cot", new FunctionDef(1, args -> 1.0 / Math.tan(args[0])));
        
        // Логарифмы
        functions.put("ln", new FunctionDef(1, args -> Math.log(args[0])));
        functions.put("log", new FunctionDef(1, args -> Math.log10(args[0])));
        
        // Алгебраические функции
        functions.put("sqrt", new FunctionDef(1, args -> Math.sqrt(args[0])));
        functions.put("√", new FunctionDef(1, args -> Math.sqrt(args[0])));
        functions.put("abs", new FunctionDef(1, args -> Math.abs(args[0])));
        functions.put("exp", new FunctionDef(1, args -> Math.exp(args[0])));
        
        // Многоаргументные функции
        functions.put("max", new FunctionDef(2, args -> Math.max(args[0], args[1])));
        functions.put("min", new FunctionDef(2, args -> Math.min(args[0], args[1])));
        
        return functions;
    }
    
    /**
     * Инициализирует операторы по умолчанию.
     */
    private static Map<String, OperatorDef> initOperators() {
        Map<String, OperatorDef> operators = new HashMap<>();
        
        operators.put("+", new OperatorDef(2, args -> args[0] + args[1]));
        operators.put("-", new OperatorDef(2, args -> args[0] - args[1]));
        operators.put("*", new OperatorDef(2, args -> args[0] * args[1]));
        operators.put("/", new OperatorDef(2, args -> {
            if (Math.abs(args[1]) < 1e-10) {
                throw new ArithmeticException("Деление на ноль");
            }
            return args[0] / args[1];
        }));
        operators.put("^", new OperatorDef(2, args -> Math.pow(args[0], args[1])));
        
        // Унарный минус
        operators.put("~", new OperatorDef(1, args -> -args[0]));
        
        return operators;
    }
}
