package ru.naumen.exception;

/**
 * Исключение, когда пароль не найден
 */
public class PasswordNotFoundException extends Exception {
    public PasswordNotFoundException(String message) {
        super(message);
    }
}