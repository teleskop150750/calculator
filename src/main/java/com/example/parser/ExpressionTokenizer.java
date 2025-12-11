package com.example.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Токенизатор математических выражений.
 * <p>
 * Разбивает входное выражение на последовательность токенов (лексем) для
 * последующего синтаксического анализа и вычисления.
 * </p>
 * 
 * <h3>Поддерживаемые токены:</h3>
 * <ul>
 * <li>Числа: целые и дробные (например, 123, 45.67)</li>
 * <li>Операторы: +, -, *, /, ^</li>
 * <li>Функции: sin, cos, tan, ln, log, sqrt и др.</li>
 * <li>Константы: pi, e</li>
 * <li>Скобки: (, )</li>
 * <li>Разделитель: , (для функций с несколькими аргументами)</li>
 * <li>Переменные: идентификаторы (a-z, A-Z, цифры, подчёркивание)</li>
 * </ul>
 */
public class ExpressionTokenizer {
    
    private final Pattern tokenPattern;
    private final Map<String, TokenType> keywords;
    
    /**
     * Создаёт токенизатор с заданными функциями и константами.
     * 
     * @param functions список имён поддерживаемых функций
     * @param constants список имён поддерживаемых констант
     */
    public ExpressionTokenizer(List<String> functions, List<String> constants) {
        this.keywords = buildKeywordsMap(functions, constants);
        this.tokenPattern = buildTokenPattern(functions, constants);
    }
    
    /**
     * Разбивает выражение на список токенов.
     * 
     * @param expression математическое выражение
     * @return список токенов
     * @throws IllegalArgumentException если обнаружены недопустимые символы
     */
    public List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = tokenPattern.matcher(expression);
        
        while (matcher.find()) {
            Token token = matchToToken(matcher);
            tokens.add(token);
        }
        
        // Проверка на неизвестные токены (игнорируем пробелы)
        List<Token> unknownTokens = new ArrayList<>();
        for (Token token : tokens) {
            if (token.getType() == TokenType.UNKNOWN) {
                unknownTokens.add(token);
            }
        }
        
        if (!unknownTokens.isEmpty()) {
            StringBuilder sb = new StringBuilder("Недопустимые символы: ");
            for (int i = 0; i < unknownTokens.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("'").append(unknownTokens.get(i).getValue()).append("'");
            }
            throw new IllegalArgumentException(sb.toString());
        }
        
        return tokens;
    }
    
    /**
     * Преобразует результат совпадения регулярного выражения в токен.
     */
    private Token matchToToken(Matcher matcher) {
        String value = matcher.group();
        int start = matcher.start();
        int end = matcher.end();
        
        // Определяем тип токена
        TokenType type = classifyToken(value);
        
        return new Token(type, value, start, end);
    }
    
    /**
     * Классифицирует токен по его значению.
     */
    private TokenType classifyToken(String value) {
        // Скобки
        if ("(".equals(value)) return TokenType.LEFT_PAREN;
        if (")".equals(value)) return TokenType.RIGHT_PAREN;
        
        // Разделитель
        if (",".equals(value)) return TokenType.DELIMITER;
        
        // Операторы
        if (value.matches("[-+*/^×÷]")) return TokenType.OPERATOR;
        
        // Числа
        if (value.matches("\\d+(\\.\\d+)?")) return TokenType.NUMBER;
        
        // Ключевые слова (функции и константы)
        TokenType keywordType = keywords.get(value.toLowerCase());
        if (keywordType != null) return keywordType;
        
        // Переменные
        if (value.matches("[a-zA-Z_]\\w*")) return TokenType.VARIABLE;
        
        // Неизвестный токен
        return TokenType.UNKNOWN;
    }
    
    /**
     * Создаёт карту ключевых слов (функции и константы).
     */
    private Map<String, TokenType> buildKeywordsMap(List<String> functions, List<String> constants) {
        Map<String, TokenType> map = new HashMap<>();
        
        for (String func : functions) {
            map.put(func.toLowerCase(), TokenType.FUNCTION);
        }
        
        for (String constant : constants) {
            map.put(constant.toLowerCase(), TokenType.CONSTANT);
        }
        
        return map;
    }
    
    /**
     * Создаёт регулярное выражение для токенизации.
     */
    private Pattern buildTokenPattern(List<String> functions, List<String> constants) {
        StringBuilder pattern = new StringBuilder();
        
        // Порядок важен! Более специфичные паттерны должны идти первыми
        
        // Скобки
        pattern.append("\\(|\\)|");
        
        // Разделитель
        pattern.append(",|");
        
        // Операторы (включая × и ÷)
        pattern.append("[-+*/^×÷]|");
        
        // Функции
        if (!functions.isEmpty()) {
            pattern.append("(?i)\\b(");
            for (int i = 0; i < functions.size(); i++) {
                if (i > 0) pattern.append("|");
                pattern.append(Pattern.quote(functions.get(i)));
            }
            pattern.append(")\\b|");
        }
        
        // Константы
        if (!constants.isEmpty()) {
            pattern.append("(?i)\\b(");
            for (int i = 0; i < constants.size(); i++) {
                if (i > 0) pattern.append("|");
                pattern.append(Pattern.quote(constants.get(i)));
            }
            pattern.append(")\\b|");
        }
        
        // Числа (целые и дробные)
        pattern.append("\\d+(?:\\.\\d+)?|");
        
        // Переменные
        pattern.append("[a-zA-Z_]\\w*|");
        
        // Пробелы (игнорируем)
        pattern.append("\\s+|");
        
        // Любой другой символ (неизвестный токен)
        pattern.append("\\S");
        
        return Pattern.compile(pattern.toString());
    }
}
