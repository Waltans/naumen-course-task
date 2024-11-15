package ru.naumen.exception;

/**
 * Ошибка некорректного типа сортировки
 */
public class IncorrectSortTypeException extends Exception {
    public IncorrectSortTypeException(String message) {
        super(message);
    }
}
