package com.example.expression;

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
        // Инициализируем компоненты из единого реестра
        Map<String, Double> constants = ExpressionDefinitions.CONSTANTS;
        Map<String, ExpressionEvaluator.FunctionDef> functions = ExpressionDefinitions.FUNCTIONS;
        Map<String, ExpressionEvaluator.OperatorDef> operators = ExpressionDefinitions.OPERATORS;
        
        // Создаём токенизатор
        ExpressionTokenizer tokenizer = new ExpressionTokenizer(
            ExpressionDefinitions.FUNCTION_NAMES,
            ExpressionDefinitions.CONSTANT_NAMES
        );
        
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
    
}
