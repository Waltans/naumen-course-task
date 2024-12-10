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
    public static final String INDEX_ERROR_MESSAGE = "Индекс должен быть числом";
    public static final String DAYS_ERROR_MESSAGE = "Напоминание можно установить на срок от 3 до 90 дней";
    public static final String USER_NOT_FOUND = "Пользователь не найден";
    public static final String ENCRYPT_ERROR = "Ошибка шифрования";


    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Errors() {

    }
}
