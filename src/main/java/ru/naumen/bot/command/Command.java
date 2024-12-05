package ru.naumen.bot.command;

/**
 * Команды, их подпись на клавиатуре и количество допустимых параметров для каждой команды
 */
public enum Command {
    /**
     * Команда генерации пароля
     */
    GENERATE("/generate", "Генерировать"),

    /**
     * Команда редактирования пароля
     */
    EDIT("/edit", "Изменить"),

    /**
     * Команда удаления пароля
     */
    DELETE("/del", "Удалить"),

    /**
     * Команда сохранения пароля
     */
    SAVE("/save", "Сохранить"),

    /**
     * Команда вызова списка паролей
     */
    LIST("/list", "Менеджер"),

    /**
     * Команда справки
     */
    HELP("/help", "Помощь"),

    /**
     * Команда запуска бота и вызова главного меню
     */
    START("/start", "Меню"),

    /**
     * Команда сортировки паролей
     */
    SORT("/sort", "Сортировать"),

    /**
     * Команда поиска паролей
     */
    FIND("/find", "Искать");

    /**
     * Команда
     */
    private final String command;

    /**
     * Подпись на клавиатуре
     */
    private final String keyboardLabel;

    /**
     * Получить команду
     */
    public String getCommand() {
        return command;
    }

    /**
     * Получить подпись команды на клавиатуре
     */
    public String getKeyboardLabel() {
        return keyboardLabel;
    }

    /**
     * Конструктор для команды, ее подписи на клавиатуре и количества параметров
     *
     * @param command          Текст команды
     * @param keyboardLabel    Описание команды
     * @param validParamCounts допустимое количество парметров
     */
    Command(String command, String keyboardLabel) {
        this.command = command;
        this.keyboardLabel = keyboardLabel;
    }
}
