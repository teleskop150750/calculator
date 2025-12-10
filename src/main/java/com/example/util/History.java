package com.example.util;

import java.util.LinkedList;

/**
 * Класс для хранения истории вычислений калькулятора.
 * <p>
 * Хранит ограниченное количество последних вычислений и предоставляет
 * методы для добавления новых записей и получения истории в виде строки.
 * </p>
 */
public class History {

    /** Максимальное количество записей в истории. */
    private static final int MAX_HISTORY_SIZE = 3;

    /** Список хранения истории вычислений. */
    private final LinkedList<String> history = new LinkedList<>();

    /**
     * Добавляет новую запись в историю.
     * <p>
     * Если количество записей превышает максимальное, удаляет самую старую.
     * </p>
     * 
     * @param entry строка с выражением и результатом
     */
    public void addEntry(String entry) {
        if (history.size() == MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
        history.addLast(entry);
    }

    /**
     * Возвращает историю в виде строки для отображения.
     * <p>
     * История выводится в порядке от старой к новой записи.
     * </p>
     * 
     * @return строка с историей вычислений
     */
    public String getFormattedHistory() {
        StringBuilder sb = new StringBuilder();

        for (String line : history) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(line);
        }

        return sb.toString();
    }

    /**
     * Очищает всю историю вычислений.
     */
    public void clear() {
        history.clear();
    }

    /**
     * Проверяет, пуста ли история.
     * 
     * @return true, если история пуста
     */
    public boolean isEmpty() {
        return history.isEmpty();
    }
}
