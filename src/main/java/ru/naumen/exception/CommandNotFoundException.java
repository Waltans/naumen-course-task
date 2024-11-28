package ru.naumen.exception;

/**
 * Исключение, когда команда не найдена
 */
public class CommandNotFoundException extends Exception {
    public CommandNotFoundException(String message) {
        super(message);
    }
}
