package ru.naumen.exception;

/**
 *  Ошибка при дешифровании пароля
 */
public class DecryptException extends RuntimeException {
    public DecryptException(String message, Throwable cause) {
        super(message);
    }
}