package com.example.parser;

import com.example.evaluator.ExpressionEvaluator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Парсер и вычислитель математических выражений.
 * <p>
 * Объединяет токенизатор, парсер Пратта и вычислитель RPN для полного
 * цикла обработки математических выражений.
 * </p>
 * 
 * <h3>Архитектура (аналогична JS-версии):</h3>
 * <ol>
 * <li><b>ExpressionTokenizer</b> - разбивает строку на токены</li>
 * <li><b>PrattParser</b> - преобразует токены в RPN</li>
 * <li><b>ExpressionEvaluator</b> - вычисляет RPN</li>
 * </ol>
 * 
 * <h3>Пример использования:</h3>
 * <pre>
 * ExpressionParser parser = new ExpressionParser("2 + 3 * sin(pi / 2)");
 * double result = parser.evaluate();  // 5.0
 * </pre>
 */
public class ExpressionParser {
    
    private final ExpressionEvaluator evaluator;
    private final List<Token> rpn;
    private final Map<String, Double> variables;
    
    /**
     * Создаёт парсер для указанного выражения.
     * 
     * @param expression математическое выражение
     * @throws IllegalArgumentException при ошибках синтаксиса
     */
    public ExpressionParser(String expression) {
        // Инициализируем компоненты
        Map<String, Double> constants = initConstants();
        Map<String, ExpressionEvaluator.FunctionDef> functions = initFunctions();
        Map<String, ExpressionEvaluator.OperatorDef> operators = initOperators();
        
        // Создаём токенизатор
        List<String> functionNames = Arrays.asList(
            "sin", "cos", "tan", "cot", "ln", "log", "sqrt", "abs", "exp", "max", "min"
        );
        List<String> constantNames = Arrays.asList("pi", "π", "e");
        
        ExpressionTokenizer tokenizer = new ExpressionTokenizer(functionNames, constantNames);
        
        // Создаём парсер
        PrattParser parser = new PrattParser(functions);
        
        // Создаём вычислитель
        this.evaluator = new ExpressionEvaluator(constants, functions, operators);
        
        // Парсим выражение
        List<Token> tokens = tokenizer.tokenize(expression);
        this.rpn = parser.parse(tokens);
        
        // Инициализируем переменные
        this.variables = new HashMap<>();
    }
    
    /**
     * Устанавливает значение переменной.
     * 
     * @param name имя переменной
     * @param value значение
     */
    public void setVariable(String name, double value) {
        variables.put(name, value);
    }
    
    /**
     * Вычисляет выражение.
     * 
     * @return результат вычисления
     * @throws IllegalArgumentException при ошибках вычисления
     */
    public double evaluate() {
        return evaluator.evaluate(rpn, variables);
    }
    
    /**
     * Возвращает RPN для отладки.
     */
    public List<Token> getRPN() {
        return rpn;
    }
    
    /**
     * Инициализирует функции по умолчанию.
     */
    private static Map<String, ExpressionEvaluator.FunctionDef> initFunctions() {
        Map<String, ExpressionEvaluator.FunctionDef> functions = new HashMap<>();
        
        // Тригонометрические функции (в радианах для внутренних вычислений)
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
        
        // Логарифмы
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
        
        // Алгебраические функции
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
        
        // Многоаргументные функции
        functions.put("max", new ExpressionEvaluator.FunctionDef(2, args -> Math.max(args[0], args[1])));
        functions.put("min", new ExpressionEvaluator.FunctionDef(2, args -> Math.min(args[0], args[1])));
        
        return functions;
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
     * Инициализирует операторы по умолчанию.
     */
    private static Map<String, ExpressionEvaluator.OperatorDef> initOperators() {
        Map<String, ExpressionEvaluator.OperatorDef> operators = new HashMap<>();
        
        operators.put("+", new ExpressionEvaluator.OperatorDef(2, args -> args[0] + args[1]));
        operators.put("-", new ExpressionEvaluator.OperatorDef(2, args -> args[0] - args[1]));
        operators.put("*", new ExpressionEvaluator.OperatorDef(2, args -> args[0] * args[1]));
        operators.put("/", new ExpressionEvaluator.OperatorDef(2, args -> {
            if (Math.abs(args[1]) < 1e-10) {
                throw new ArithmeticException("Деление на ноль");
            }
            return args[0] / args[1];
        }));
        operators.put("^", new ExpressionEvaluator.OperatorDef(2, args -> Math.pow(args[0], args[1])));
        
        // Унарный минус
        operators.put("~", new ExpressionEvaluator.OperatorDef(1, args -> -args[0]));
        
        return operators;
    }
}
