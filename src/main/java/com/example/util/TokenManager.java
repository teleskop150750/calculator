package com.example.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Управляет списком токенов математического выражения.
 * <p>
 * Предоставляет методы для добавления, удаления и проверки токенов,
 * а также для формирования строк выражения для отображения и вычисления.
 * </p>
 */
public class TokenManager {

    /** Паттерн для распознавания операторов. */
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("[+\\-*/^]");

    /** Паттерн для распознавания чисел. */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

    /** Список токенов выражения. */
    private final List<String> tokens = new ArrayList<>();

    /**
     * Добавляет токен в конец списка.
     * 
     * @param token добавляемый токен
     */
    public void add(String token) {
        tokens.add(token);
    }

    /**
     * Удаляет последний токен из списка.
     */
    public void removeLast() {
        if (!tokens.isEmpty()) {
            tokens.remove(tokens.size() - 1);
        }
    }

    /**
     * Заменяет последний оператор новым или добавляет его.
     * <p>
     * Если последний токен — оператор, заменяет его.
     * Иначе добавляет новый оператор в конец.
     * </p>
     * 
     * @param newOperator новый оператор
     */
    public void replaceOrAddOperator(String newOperator) {
        if (isOperator(getLast())) {
            tokens.set(tokens.size() - 1, newOperator);
        } else {
            tokens.add(newOperator);
        }
    }

    /**
     * Возвращает последний токен без удаления.
     * 
     * @return последний токен или пустая строка
     */
    public String getLast() {
        return tokens.isEmpty() ? "" : tokens.get(tokens.size() - 1);
    }

    /**
     * Удаляет и возвращает последний токен.
     *
     * @return удалённый токен или пустая строка
     */
    public String getLastRemoved() {
        return tokens.isEmpty() ? "" : tokens.remove(tokens.size() - 1);
    }

    /**
     * Проверяет, пуст ли список токенов.
     * 
     * @return true, если токенов нет
     */
    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    /**
     * Очищает все токены.
     */
    public void clear() {
        tokens.clear();
    }

    // /**
    // * Возвращает копию списка токенов.
    // *
    // * @return список токенов
    // */
    // public List<String> getTokens() {
    // return new ArrayList<>(tokens);
    // }

    /**
     * Формирует выражение для вычисления (без пробелов).
     *
     * @return строка выражения
     */
    public String toExpression() {
        return String.join("", tokens);
    }

    /**
     * Формирует выражение для отображения (с пробелами).
     *
     * @return строка для показа пользователю
     */
    public String toDisplayString() {
        return String.join(" ", tokens);
    }

    /**
     * Проверяет, является ли токен оператором.
     * 
     * @param token проверяемый токен
     * @return true, если токен — оператор (+, -, *, /, ^)
     */
    public boolean isOperator(String token) {
        return OPERATOR_PATTERN.matcher(token).matches();
    }

    /**
     * Удаляет оператор в конце выражения перед вычислением.
     */
    public void trailingOperator() {
        if (isOperator(getLast())) {
            removeLast();
        }
    }

    /**
     * Проверяет, является ли токен числом.
     *
     * @param token проверяемый токен
     * @return true, если токен — число
     */
    public boolean isNumber(String token) {
        return NUMBER_PATTERN.matcher(token).matches();
    }

    /**
     * Проверяет, является ли токен специальным (оператор, скобка, константа).
     *
     * @param token проверяемый токен
     * @return true, если токен специальный
     */
    public boolean isSpecialToken(String token) {
        return isOperator(token) ||
                "(".equals(token) ||
                ")".equals(token) ||
                ",".equals(token) ||
                "pi".equals(token) ||
                "e".equals(token);
    }

    // /**
    // * Проверяет, может ли следующий минус быть унарным.
    // * <p>
    // * Унарный минус возможен в начале выражения, после открывающей
    // * скобки или после другого оператора.
    // * </p>
    // *
    // * @return true, если унарный минус допустим
    // */
    // public boolean canBeUnaryMinus() {
    // return "(".equals(getLast()) ||
    // isOperator(getLast());
    // }
}
