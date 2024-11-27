package ru.naumen.exception;

/**
 * Ошибка, которая возникает при шифровании пароля
 */
public class EncryptException extends RuntimeException {
    public EncryptException(String message, Throwable cause) {
        super(message);
    }
}
