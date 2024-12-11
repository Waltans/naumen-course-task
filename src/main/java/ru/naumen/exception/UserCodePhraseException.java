package ru.naumen.exception;

/**
 * Ошибка в случае, если пользователь не может поменять кодовое слово
 */
public class UserCodePhraseException extends Exception {
    public UserCodePhraseException(String format) {
        super(format);
    }
}
