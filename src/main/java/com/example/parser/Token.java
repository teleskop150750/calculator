package com.example.parser;

/**
 * Представляет токен (лексему) математического выражения.
 * <p>
 * Токен содержит тип (число, оператор, функция и т.д.), значение и позицию
 * в исходном выражении для точной диагностики ошибок.
 * </p>
 */
public class Token {
    private final TokenType type;
    private final String value;
    private final int start;
    private final int end;

    /**
     * Создаёт новый токен.
     * 
     * @param type тип токена
     * @param value строковое значение токена
     * @param start начальная позиция в выражении
     * @param end конечная позиция в выражении
     */
    public Token(TokenType type, String value, int start, int end) {
        this.type = type;
        this.value = value;
        this.start = start;
        this.end = end;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("Token{type=%s, value='%s', pos=%d:%d}", 
            type, value, start, end);
    }
}
