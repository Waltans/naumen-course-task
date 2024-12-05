package ru.naumen.bot.constants;

/**
 * Константы - параметры
 */
public class Parameters {
    public static final int COMMAND_WITHOUT_PARAMS_LENGTH = 1;
    public static final int MINIMUM_PASSWORD_LENGTH = 8;
    public static final int MAXIMUM_PASSWORD_LENGTH = 128;
    public static final String COMPLEXITY_EASY = "Простой";
    public static final String COMPLEXITY_MEDIUM = "Средний";
    public static final String COMPLEXITY_HARD = "Сложный";
    public static final String BY_DATE = "Дате";
    public static final String BY_DESCRIPTION = "Описанию";

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Parameters() {

    }
}
