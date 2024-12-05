package ru.naumen.exception;

/**
 * Ошибка, если введена неверная сложность пароля
 */
public class ComplexityFormatException extends Exception {
    public ComplexityFormatException(String message) {
        super(message);
    }
}
