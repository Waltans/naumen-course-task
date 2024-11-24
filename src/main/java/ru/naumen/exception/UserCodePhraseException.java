package ru.naumen.exception;

public class UserCodePhraseException extends Exception {
    public UserCodePhraseException(String format) {
        super(format);
    }
}
