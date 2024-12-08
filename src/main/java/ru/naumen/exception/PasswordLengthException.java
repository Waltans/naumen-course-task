package ru.naumen.exception;

/**
 * Ошибка в случае, если длина пароля задана неверно
 */
public class PasswordLengthException extends Exception {
    public PasswordLengthException(String message) {
        super(message);
    }
}
