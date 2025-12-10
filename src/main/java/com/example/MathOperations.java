package com.example;

/**
 * Класс для выполнения математических операций.
 * <p>
 * Предоставляет статические методы для унарных и бинарных арифметических
 * операций,
 * включая тригонометрические функции, логарифмы, факториал и возведение в
 * степень.
 * </p>
 */
public class MathOperations {

    /** Малый порог для сравнения чисел с плавающей точкой. */
    private static final double EPSILON = 1e-10;

    /**
     * Применяет унарную математическую функцию к указанному значению.
     * <p>
     * Поддерживаемые функции включают тригонометрические (sin, cos, tan, cot),
     * логарифмические (ln, log), алгебраические (sqrt, abs, exp) и факториал.
     * </p>
     * 
     * <h3>Обработка ошибок:</h3>
     * <ul>
     * <li><b>Тригонометрия:</b> cot(90°) и подобные углы вызывают ошибку деления на
     * ноль</li>
     * <li><b>Логарифмы:</b> отрицательные и нулевые аргументы недопустимы</li>
     * <li><b>Корень:</b> отрицательные числа вызывают ошибку (комплексные числа не
     * поддерживаются)</li>
     * <li><b>Факториал:</b> только неотрицательные целые числа до 20 (защита от
     * переполнения)</li>
     * </ul>
     * 
     * @param fn название функции (например, "sin", "ln", "√")
     * @param v  аргумент функции
     * @return результат вычисления
     * @throws IllegalArgumentException при некорректных аргументах
     */
    public static double applyUnaryFunction(String fn, double v) {
        try {
            switch (fn) {
                case "sin":
                    return Math.sin(Math.toRadians(v));
                case "cos":
                    return Math.cos(Math.toRadians(v));
                case "tan":
                    return Math.tan(Math.toRadians(v));
                case "cot":
                    double tanVal = Math.tan(Math.toRadians(v));
                    if (Math.abs(tanVal) < EPSILON)
                        throw new ArithmeticException("Деление на ноль при вычислении котангенса");
                    return 1.0 / tanVal;
                case "ln":
                    if (v <= 0)
                        throw new IllegalArgumentException("Логарифм определён только для положительных чисел");
                    return Math.log(v);
                case "log":
                    if (v <= 0)
                        throw new IllegalArgumentException("Логарифм определён только для положительных чисел");
                    return Math.log10(v);
                case "√":
                    if (v < 0)
                        throw new IllegalArgumentException("Корень из отрицательного числа не определён");
                    return Math.sqrt(v);
                case "n!":
                    if (v < 0 || v != Math.floor(v) || v > 20)
                        throw new IllegalArgumentException("Факториал определён только для целых чисел от 0 до 20");
                    return (double) factorial((int) v);
                case "abs":
                    return Math.abs(v);
                case "exp":
                    return Math.exp(v);
                default:
                    throw new UnsupportedOperationException("Неизвестная функция: " + fn);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при выполнении операции " + fn + ": " + e.getMessage());
        }
    }

    /**
     * Выполняет бинарную арифметическую операцию над двумя числами.
     * <p>
     * Поддерживает основные арифметические операции: сложение, вычитание,
     * умножение, деление и возведение в степень.
     * </p>
     * 
     * @param left  левый операнд
     * @param right правый операнд
     * @param op    оператор (+, -, ×, ÷, ^)
     * @return результат операции
     * @throws ArithmeticException           при делении на ноль
     * @throws UnsupportedOperationException при неизвестном операторе
     */
    public static double applyBinaryOperation(double left, double right, String op) {
        switch (op) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
            case "×":
                return left * right;
            case "/":
            case "÷":
                if (Math.abs(right) < EPSILON)
                    throw new ArithmeticException("Деление на ноль");
                return left / right;
            case "^":
                return Math.pow(left, right);
            default:
                throw new UnsupportedOperationException("Неизвестный оператор: " + op);
        }
    }

    /**
     * Вычисляет факториал целого неотрицательного числа.
     * <p>
     * Итеративный алгоритм более эффективен, чем рекурсивный,
     * и не вызывает переполнения стека.
     * </p>
     * 
     * @param n неотрицательное целое число от 0 до 20
     * @return n! (факториал числа n)
     */
    private static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
