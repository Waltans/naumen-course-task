package ru.naumen.exception;

public class PasswordNotFoundException extends Exception {
    public PasswordNotFoundException(String message) {
        super(message);
    }
}