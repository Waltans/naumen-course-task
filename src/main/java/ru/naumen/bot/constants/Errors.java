package ru.naumen.bot.constants;

/**
 * Константы - ошибки
 */
public class Errors {
    public static final String INCORRECT_COMMAND_RESPONSE = "Введена некорректная команда! Справка: /help";
    public static final String LENGTH_ERROR_MESSAGE = "Длина пароля должна быть от 8 до 128 символов!";
    public static final String COMPLEXITY_ERROR_MESSAGE = """
    Сложность должна быть от 1 до 3, где:
    1 - простой пароль;
    2 - пароль средней сложности;
    3 - сложный пароль.""";
    public static final String PASSWORD_NOT_FOUND_MESSAGE = "Не найден пароль с id %s";
    public static final String NO_PASSWORDS_MESSAGE = "Нет ни одного пароля. Справка: /help";

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Errors() {

    }
}
