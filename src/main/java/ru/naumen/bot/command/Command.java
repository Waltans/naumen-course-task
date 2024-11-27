package ru.naumen.bot.command;

import java.util.List;

/**
 * Команды, их подпись на клавиатуре и количество допустимых параметров для каждой команды
 */
public enum Command {
    /**
     * Команда генерации пароля
     */
    GENERATE("/generate", "Генерировать", List.of(2, 0)),

    /**
     * Команда редактирования пароля
     */
    EDIT("/edit", "Изменить", List.of(3, 4, 0)),

    /**
     * Команда удаления пароля
     */
    DELETE("/del", "Удалить", List.of(1, 0)),

    /**
     * Команда сохранения пароля
     */
    SAVE("/save", "Сохранить", List.of(1, 2, 0)),

    /**
     * Команда вызова списка паролей
     */
    LIST("/list", "Менеджер", List.of(0)),

    /**
     * Команда справки
     */
    HELP("/help", "Помощь", List.of(0)),

    /**
     * Команда запуска бота и вызова главного меню
     */
    START("/start", "Меню", List.of(0)),

    /**
     * Команда сортировки паролей
     */
    SORT("/sort", "Сортировать", List.of(0)),

    /**
     * Команда поиска паролей
     */
    FIND("/find", "Искать", List.of(1, 0));

    /**
     * Команда
     */
    private final String command;

    /**
     * Подпись на клавиатуре
     */
    private final String keyboardLabel;

    /**
     * Допустимое количество параметров
     */
    private final List<Integer> validParamCounts;

    /**
     * Конструктор для команды, ее подписи на клавиатуре и количества параметров
     *
     * @param command     Текст команды
     * @param keyboardLabel Описание команды
     * @param validParamCounts допустимое количество парметров
     */
    Command(String command, String keyboardLabel, List<Integer> validParamCounts) {
        this.command = command;
        this.keyboardLabel = keyboardLabel;
        this.validParamCounts = validParamCounts;
    }

    public String getCommand() {
        return command;
    }

    public String getKeyboardLabel() {
        return keyboardLabel;
    }

    public List<Integer> getValidParamCounts() {
        return validParamCounts;
    }

    /**
     * Проверяет, соответствует ли введённая строка команде.
     * Сравнивает как команду, так и подпись на клавиатуре.
     *
     * @param input ввод
     * @return true, если строка соответствует команде или подписи
     */
    boolean isCommandMatches(String input) {
        return this.command.equalsIgnoreCase(input)
                || this.keyboardLabel.equalsIgnoreCase(input);
    }
}
