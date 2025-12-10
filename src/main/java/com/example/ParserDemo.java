package com.example;

/**
 * Демонстрация работы нового парсера выражений.
 * <p>
 * Этот класс показывает возможности парсера на основе алгоритма Пратта
 * и сравнивает его с предыдущей реализацией.
 * </p>
 */
public class ParserDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Демонстрация ExpressionParser ===\n");
        
        // Простые выражения
        testExpression("2 + 3 * 4");
        testExpression("(2 + 3) * 4");
        testExpression("2 ^ 3 ^ 2");  // Правоассоциативность
        
        // Функции
        testExpression("sin(pi / 2)");
        testExpression("log(100)");
        testExpression("sqrt(16)");
        testExpression("max(5, 3)");
        
        // Сложные выражения
        testExpression("2 + 3 * sin(pi / 6) - sqrt(16) / 2");
        testExpression("(2 + 3) * (4 - 1) ^ 2");
        
        // Константы
        testExpression("pi * e");
        testExpression("2 * π");
        
        // Унарный минус
        testExpression("-5 + 3");
        testExpression("2 * -3");
        testExpression("-(2 + 3)");
        
        // Переменные
        testExpressionWithVariable("x + y", "x", "5", "y", "3");
        testExpressionWithVariable("2 * x ^ 2 + 3 * x + 1", "x", "4");
        
        System.out.println("\n=== Тесты завершены ===");
    }
    
    private static void testExpression(String expression) {
        try {
            ExpressionParser parser = new ExpressionParser(expression);
            double result = parser.evaluate();
            System.out.printf("✓ %-40s = %s%n", expression, NumberFormatter.format(result));
            
            // Показываем RPN для интереса
            if (expression.length() < 20) {
                System.out.printf("  RPN: %s%n", rpnToString(parser.getRPN()));
            }
        } catch (Exception e) {
            System.out.printf("✗ %-40s ошибка: %s%n", expression, e.getMessage());
        }
    }
    
    private static void testExpressionWithVariable(String expression, String... varsAndValues) {
        try {
            ExpressionParser parser = new ExpressionParser(expression);
            
            // Устанавливаем переменные
            StringBuilder varInfo = new StringBuilder();
            for (int i = 0; i < varsAndValues.length; i += 2) {
                String varName = varsAndValues[i];
                double varValue = Double.parseDouble(varsAndValues[i + 1]);
                parser.setVariable(varName, varValue);
                if (varInfo.length() > 0) varInfo.append(", ");
                varInfo.append(varName).append("=").append(varValue);
            }
            
            double result = parser.evaluate();
            System.out.printf("✓ %-25s  [%s] = %s%n", expression, varInfo, NumberFormatter.format(result));
        } catch (Exception e) {
            System.out.printf("✗ %-25s  ошибка: %s%n", expression, e.getMessage());
        }
    }
    
    private static String rpnToString(java.util.List<Token> rpn) {
        StringBuilder sb = new StringBuilder();
        for (Token token : rpn) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(token.getValue());
        }
        return sb.toString();
    }
}
