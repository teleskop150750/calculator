package com.example.expression;

/**
 * Типы токенов математического выражения.
 */
public enum TokenType {
    /** Число (целое или дробное) */
    NUMBER,
    
    /** Математическая константа (π, e) */
    CONSTANT,
    
    /** Переменная */
    VARIABLE,
    
    /** Оператор (+, -, *, /, ^) */
    OPERATOR,
    
    /** Функция (sin, cos, ln и т.д.) */
    FUNCTION,
    
    /** Открывающая скобка '(' */
    LEFT_PAREN,
    
    /** Закрывающая скобка ')' */
    RIGHT_PAREN,
    
    /** Разделитель аргументов ',' */
    DELIMITER,
    
    /** Неизвестный токен (ошибка) */
    UNKNOWN
}
