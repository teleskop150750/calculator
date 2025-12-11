package com.example.util;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;

/**
 * Класс для управления текущим вводом числа пользователем.
 * <p>
 * Инкапсулирует логику накопления символов числа, включая
 * десятичные разделители, унарный минус и операции очистки.
 * </p>
 */
public class CurrentInput {

    /** Текущая строка ввода. */
    private String value = "";

    private TokenManager tokenManager;

    /**
     * Создаёт новый пустой ввод.
     */
    public CurrentInput(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Устанавливает новое значение.
     * 
     * @param value новое значение
     */
    public void setValue(String value) {
        this.value = value != null ? value : "";
    }

    public void onDigit(String value) {
        if (this.tokenManager.isStartNewNumber()) {
            this.value = value;
            this.tokenManager.setStartNewNumber(false);
        } else {
            this.value += value;
        }
    }

    public void onDecimalPoint() {
        if (this.tokenManager.isStartNewNumber()) {
            this.value = "0.";
            this.tokenManager.setStartNewNumber(false);
        } else {
            if (this.value.isEmpty()) {
                this.value = "0.";
            } else if (!this.value.contains(".")) {
                this.value += ".";
            }
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

    // /**
    // * Проверяет, содержит ли ввод десятичный разделитель.
    // *
    // * @return true, если содержит точку
    // */
    // public boolean hasDecimalPoint() {
    // return value.contains(".");
    // }

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

    // /**
    // * Возвращает длину текущего ввода.
    // *
    // * @return количество символов
    // */
    // public int length() {
    // return value.length();
    // }

    // /**
    // * Проверяет, является ли текущий ввод числом.
    // *
    // * @return true, если ввод соответствует формату числа
    // */
    // public boolean isNumber() {
    // return value.matches("-?\\d+(\\.\\d+)?");
    // }

    // @Override
    // public String toString() {
    // return value;
    // }
}
