package com.example.util;

/**
 * Класс для управления текущим вводом числа пользователем.
 * <p>
 * Инкапсулирует логику накопления символов числа, включая
 * десятичные разделители, унарный минус и операции очистки.
 * </p>
 */
public class CurrentInput {
    
    /** Текущая строка ввода. */
    private String value;
    
    /**
     * Создаёт новый пустой ввод.
     */
    public CurrentInput() {
        this.value = "";
    }
    
    /**
     * Создаёт ввод с начальным значением.
     * 
     * @param initialValue начальное значение
     */
    public CurrentInput(String initialValue) {
        this.value = initialValue != null ? initialValue : "";
    }
    
    /**
     * Возвращает текущее значение.
     * 
     * @return строка текущего ввода
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Устанавливает новое значение.
     * 
     * @param value новое значение
     */
    public void setValue(String value) {
        this.value = value != null ? value : "";
    }
    
    /**
     * Добавляет символ к текущему вводу.
     * 
     * @param digit символ для добавления
     */
    public void appendDigit(String digit) {
        this.value += digit;
    }
    
    /**
     * Добавляет десятичный разделитель, если его ещё нет.
     * Если ввод пустой, начинает с "0."
     */
    public void appendDecimalPoint() {
        if (value.isEmpty()) {
            value = "0.";
        } else if (!value.contains(".")) {
            value += ".";
        }
    }
    
    /**
     * Проверяет, пустой ли текущий ввод.
     * 
     * @return true, если ввод пустой
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }
    
    /**
     * Проверяет, содержит ли ввод десятичный разделитель.
     * 
     * @return true, если содержит точку
     */
    public boolean hasDecimalPoint() {
        return value.contains(".");
    }
    
    /**
     * Очищает текущий ввод.
     */
    public void clear() {
        value = "";
    }
    
    /**
     * Удаляет последний символ из ввода.
     * Если после удаления ввод становится пустым, устанавливает "0".
     * 
     * @return true, если символ был удалён
     */
    public boolean backspace() {
        if (value.length() > 0) {
            value = value.substring(0, value.length() - 1);
            if (value.isEmpty()) {
                value = "0";
            }
            return true;
        }
        return false;
    }
    
    /**
     * Возвращает длину текущего ввода.
     * 
     * @return количество символов
     */
    public int length() {
        return value.length();
    }
    
    /**
     * Проверяет, является ли текущий ввод числом.
     * 
     * @return true, если ввод соответствует формату числа
     */
    public boolean isNumber() {
        return value.matches("-?\\d+(\\.\\d+)?");
    }
    
    @Override
    public String toString() {
        return value;
    }
}
