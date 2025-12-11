package com.example.expression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry of supported math tokens: constants, functions, operators and their precedence.
 * Keeping them in one place avoids string literals being scattered across tokenizer/parser/evaluator.
 */
public final class ExpressionDefinitions {

    private ExpressionDefinitions() {
    }

    public static final Map<String, Double> CONSTANTS;
    public static final Map<String, ExpressionEvaluator.FunctionDef> FUNCTIONS;
    public static final Map<String, ExpressionEvaluator.OperatorDef> OPERATORS;
    public static final Map<String, Integer> OPERATOR_PRECEDENCE;
    public static final List<String> FUNCTION_NAMES;
    public static final List<String> CONSTANT_NAMES;

    static {
        Map<String, Double> constants = new LinkedHashMap<>();
        constants.put("pi", Math.PI);
        constants.put("π", Math.PI);
        constants.put("e", Math.E);
        CONSTANTS = Collections.unmodifiableMap(constants);
        CONSTANT_NAMES = List.copyOf(constants.keySet());

        Map<String, ExpressionEvaluator.FunctionDef> functions = new LinkedHashMap<>();
        // Trigonometric
        functions.put("sin", new ExpressionEvaluator.FunctionDef(1, args -> Math.sin(args[0])));
        functions.put("cos", new ExpressionEvaluator.FunctionDef(1, args -> Math.cos(args[0])));
        functions.put("tan", new ExpressionEvaluator.FunctionDef(1, args -> Math.tan(args[0])));
        functions.put("cot", new ExpressionEvaluator.FunctionDef(1, args -> {
            double tan = Math.tan(args[0]);
            if (Math.abs(tan) < 1e-10) {
                throw new ArithmeticException("Деление на ноль при вычислении котангенса");
            }
            return 1.0 / tan;
        }));

        // Logarithms
        functions.put("ln", new ExpressionEvaluator.FunctionDef(1, args -> {
            if (args[0] <= 0) {
                throw new IllegalArgumentException("Логарифм определён только для положительных чисел");
            }
            return Math.log(args[0]);
        }));
        functions.put("log", new ExpressionEvaluator.FunctionDef(1, args -> {
            if (args[0] <= 0) {
                throw new IllegalArgumentException("Логарифм определён только для положительных чисел");
            }
            return Math.log10(args[0]);
        }));

        // Algebraic
        functions.put("sqrt", new ExpressionEvaluator.FunctionDef(1, args -> {
            if (args[0] < 0) {
                throw new IllegalArgumentException("Корень из отрицательного числа не определён");
            }
            return Math.sqrt(args[0]);
        }));
        functions.put("√", new ExpressionEvaluator.FunctionDef(1, args -> {
            if (args[0] < 0) {
                throw new IllegalArgumentException("Корень из отрицательного числа не определён");
            }
            return Math.sqrt(args[0]);
        }));
        functions.put("abs", new ExpressionEvaluator.FunctionDef(1, args -> Math.abs(args[0])));
        functions.put("exp", new ExpressionEvaluator.FunctionDef(1, args -> Math.exp(args[0])));

        // Multi-argument
        functions.put("max", new ExpressionEvaluator.FunctionDef(2, args -> Math.max(args[0], args[1])));
        functions.put("min", new ExpressionEvaluator.FunctionDef(2, args -> Math.min(args[0], args[1])));

        FUNCTIONS = Collections.unmodifiableMap(functions);
        FUNCTION_NAMES = List.copyOf(functions.keySet());

        Map<String, ExpressionEvaluator.OperatorDef> operators = new LinkedHashMap<>();
        operators.put("+", new ExpressionEvaluator.OperatorDef(2, args -> args[0] + args[1]));
        operators.put("-", new ExpressionEvaluator.OperatorDef(2, args -> args[0] - args[1]));
        operators.put("*", new ExpressionEvaluator.OperatorDef(2, args -> args[0] * args[1]));
        operators.put("×", new ExpressionEvaluator.OperatorDef(2, args -> args[0] * args[1]));
        operators.put("/", new ExpressionEvaluator.OperatorDef(2, args -> {
            if (Math.abs(args[1]) < 1e-10) {
                throw new ArithmeticException("Деление на ноль");
            }
            return args[0] / args[1];
        }));
        operators.put("÷", new ExpressionEvaluator.OperatorDef(2, args -> {
            if (Math.abs(args[1]) < 1e-10) {
                throw new ArithmeticException("Деление на ноль");
            }
            return args[0] / args[1];
        }));
        operators.put("^", new ExpressionEvaluator.OperatorDef(2, args -> Math.pow(args[0], args[1])));
        operators.put("~", new ExpressionEvaluator.OperatorDef(1, args -> -args[0])); // unary minus
        OPERATORS = Collections.unmodifiableMap(operators);

        Map<String, Integer> precedence = new LinkedHashMap<>();
        precedence.put("+", 10);
        precedence.put("-", 10);
        precedence.put("*", 20);
        precedence.put("×", 20);
        precedence.put("/", 20);
        precedence.put("÷", 20);
        precedence.put("^", 30);
        precedence.put("~", 40);
        OPERATOR_PRECEDENCE = Collections.unmodifiableMap(precedence);
    }
}
