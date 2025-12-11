package com.example.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Парсер математических выражений на основе алгоритма Пратта.
 * <p>
 * Алгоритм Пратта (Pratt Parser) - это метод синтаксического анализа, который
 * элегантно обрабатывает приоритеты операторов и ассоциативность. Он работает
 * с префиксными, инфиксными и постфиксными операторами.
 * </p>
 * 
 * <h3>Преимущества алгоритма Пратта:</h3>
 * <ul>
 * <li>Компактный и понятный код</li>
 * <li>Легко добавлять новые операторы</li>
 * <li>Естественная обработка приоритетов</li>
 * <li>Поддержка право- и левоассоциативности</li>
 * </ul>
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Operator-precedence_parser#Pratt_parsing">Pratt Parsing</a>
 */
public class PrattParser {
    
    private final Map<String, Integer> precedence;
    private final Map<String, ExpressionEvaluator.FunctionDef> functions;
    private List<Token> tokens;
    private int position;
    
    /**
     * Создаёт парсер с заданными функциями.
     * 
     * @param functions карта функций и их определений
     */
    public PrattParser(Map<String, ExpressionEvaluator.FunctionDef> functions) {
        this.functions = functions;
        this.precedence = ExpressionDefinitions.OPERATOR_PRECEDENCE;
    }
    
    /**
     * Парсит список токенов и возвращает RPN (обратную польскую нотацию).
     * 
     * @param tokens список токенов
     * @return список токенов в RPN
     * @throws IllegalArgumentException при синтаксических ошибках
     */
    public List<Token> parse(List<Token> tokens) {
        this.tokens = filterWhitespace(tokens);
        this.position = 0;
        
        List<Token> rpn = new ArrayList<>();
        parseExpression(0, rpn);
        
        if (position < this.tokens.size()) {
            Token token = this.tokens.get(position);
            throw new IllegalArgumentException(
                String.format("Неожиданный токен '%s' в позиции %d:%d",
                    token.getValue(), token.getStart(), token.getEnd()));
        }
        
        return rpn;
    }
    
    /**
     * Рекурсивно парсит выражение с учётом приоритета.
     * 
     * @param minPrecedence минимальный приоритет оператора
     * @param rpn выходной список в RPN
     */
    private void parseExpression(int minPrecedence, List<Token> rpn) {
        // Парсим левый операнд (префиксное выражение)
        parsePrefix(rpn);
        
        // Обрабатываем инфиксные операторы
        while (position < tokens.size()) {
            Token token = tokens.get(position);
            
            // Проверяем, является ли токен инфиксным оператором
            if (token.getType() == TokenType.OPERATOR) {
                int prec = getPrecedence(token.getValue());
                
                if (prec < minPrecedence) {
                    break;  // Приоритет слишком низкий
                }
                
                position++;  // Потребляем оператор
                
                // Парсим правый операнд
                // Для правоассоциативных операторов используем prec, для левоассоциативных prec+1
                boolean rightAssociative = "^".equals(token.getValue());
                int nextPrec = rightAssociative ? prec : prec + 1;
                parseExpression(nextPrec, rpn);
                
                // Добавляем оператор в RPN (после операндов)
                rpn.add(token);
            } else {
                break;  // Не инфиксный оператор
            }
        }
    }
    
    /**
     * Парсит префиксное выражение (число, функция, скобки, унарный минус).
     */
    private void parsePrefix(List<Token> rpn) {
        if (position >= tokens.size()) {
            throw new IllegalArgumentException("Неожиданный конец выражения");
        }
        
        Token token = tokens.get(position++);
        
        switch (token.getType()) {
            case NUMBER:
            case CONSTANT:
            case VARIABLE:
                rpn.add(token);
                break;
                
            case FUNCTION:
                parseFunction(token, rpn);
                break;
                
            case LEFT_PAREN:
                parseParenthesizedExpression(rpn);
                break;
                
            case OPERATOR:
                // Унарный минус
                if ("-".equals(token.getValue())) {
                    parseExpression(getPrecedence("~"), rpn);
                    // Добавляем специальный токен для унарного минуса
                    rpn.add(new Token(TokenType.OPERATOR, "~", token.getStart(), token.getEnd()));
                } else if ("+".equals(token.getValue())) {
                    // Унарный плюс игнорируем
                    parseExpression(getPrecedence("~"), rpn);
                } else {
                    throw new IllegalArgumentException(
                        String.format("Неожиданный оператор '%s' в позиции %d:%d",
                            token.getValue(), token.getStart(), token.getEnd()));
                }
                break;
                
            default:
                throw new IllegalArgumentException(
                    String.format("Неожиданный токен '%s' в позиции %d:%d",
                        token.getValue(), token.getStart(), token.getEnd()));
        }
    }
    
    /**
     * Парсит вызов функции.
     */
    private void parseFunction(Token funcToken, List<Token> rpn) {
        String funcName = funcToken.getValue().toLowerCase();
        ExpressionEvaluator.FunctionDef func = functions.get(funcName);
        
        if (func == null) {
            throw new IllegalArgumentException(
                String.format("Неизвестная функция '%s' в позиции %d:%d",
                    funcToken.getValue(), funcToken.getStart(), funcToken.getEnd()));
        }
        
        // Ожидаем открывающую скобку
        if (position >= tokens.size() || tokens.get(position).getType() != TokenType.LEFT_PAREN) {
            throw new IllegalArgumentException(
                String.format("Ожидается '(' после функции '%s' в позиции %d:%d",
                    funcToken.getValue(), funcToken.getStart(), funcToken.getEnd()));
        }
        position++;  // Потребляем '('
        
        // Парсим аргументы
        int argCount = 0;
        boolean firstArg = true;
        
        while (position < tokens.size() && tokens.get(position).getType() != TokenType.RIGHT_PAREN) {
            if (!firstArg) {
                // Ожидаем запятую
                if (position >= tokens.size() || tokens.get(position).getType() != TokenType.DELIMITER) {
                    throw new IllegalArgumentException(
                        String.format("Ожидается ',' между аргументами функции '%s'",
                            funcToken.getValue()));
                }
                position++;  // Потребляем ','
            }
            firstArg = false;
            
            parseExpression(0, rpn);
            argCount++;
        }
        
        if (position >= tokens.size()) {
            throw new IllegalArgumentException(
                String.format("Ожидается ')' после аргументов функции '%s'",
                    funcToken.getValue()));
        }
        
        position++;  // Потребляем ')'
        
        // Проверяем количество аргументов
        if (argCount != func.args) {
            throw new IllegalArgumentException(
                String.format("Функция '%s' ожидает %d аргумент(ов), получено %d",
                    funcToken.getValue(), func.args, argCount));
        }
        
        // Добавляем функцию в RPN (после аргументов)
        rpn.add(funcToken);
    }
    
    /**
     * Парсит выражение в скобках.
     */
    private void parseParenthesizedExpression(List<Token> rpn) {
        parseExpression(0, rpn);
        
        if (position >= tokens.size() || tokens.get(position).getType() != TokenType.RIGHT_PAREN) {
            throw new IllegalArgumentException("Ожидается ')'");
        }
        
        position++;  // Потребляем ')'
    }
    
    /**
     * Возвращает приоритет оператора.
     */
    private int getPrecedence(String operator) {
        return precedence.getOrDefault(operator, 0);
    }
    
    /**
     * Фильтрует пробелы из списка токенов.
     */
    private List<Token> filterWhitespace(List<Token> tokens) {
        List<Token> filtered = new ArrayList<>();
        for (Token token : tokens) {
            // Пропускаем пробелы (они не имеют типа в нашей реализации)
            if (token.getType() != TokenType.UNKNOWN || !token.getValue().matches("\\s+")) {
                filtered.add(token);
            }
        }
        return filtered;
    }
    
}
