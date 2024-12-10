package ru.naumen.bot.constants;

/**
 * Константы - параметры
 */
public class Parameters {
    public static final int COMMAND_WITHOUT_PARAMS_LENGTH = 1;
    public static final String COMPLEXITY_EASY = "Простой";
    public static final String COMPLEXITY_MEDIUM = "Средний";
    public static final String COMPLEXITY_HARD = "Сложный";
    public static final String BY_DATE = "Дате";
    public static final String BY_DESCRIPTION = "Описанию";
    public static final String CLEAR_ALL_PARAM = "ALL";
    public static final int MINIMUM_DAYS_TO_REMIND = 3;
    public static final int MAXIMUM_DAYS_TO_REMIND = 90;
    public static final long MILLIS_IN_A_DAY = 60 * 60 * 24 * 1000;

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Parameters() {

    }
}
