package ru.naumen.bot;

import java.util.List;
import java.util.Map;

/**
 * Команды как константы и количество допустимых параметров для каждой команды
 */
public class Command {
    public static final String GENERATE = "/generate";
    public static final String GENERATE_KEYBOARD = "Генерировать";
    public static final String EDIT = "/edit";
    public static final String EDIT_KEYBOARD = "Изменить";
    public static final String DELETE = "/del";
    public static final String DELETE_KEYBOARD = "Удалить";
    public static final String SAVE = "/save";
    public static final String SAVE_KEYBOARD = "Сохранить";
    public static final String LIST = "/list";
    public static final String LIST_KEYBOARD = "Менеджер";
    public static final String HELP = "/help";
    public static final String HELP_KEYBOARD = "Помощь";
    public static final String START = "/start";
    public static final String SORT = "/sort";
    public static final String SORT_KEYBOARD = "Сортировать";
    public static final String FIND = "/find";
    public static final String FIND_KEYBOARD = "Искать";
    public static final String COMPLEXITY_EASY = "Простой";
    public static final String COMPLEXITY_MEDIUM = "Средний";
    public static final String COMPLEXITY_HARD = "Сложный";
    public static final String BY_DATE = "Дате";
    public static final String BY_DESCRIPTION = "Описанию";
    public static final String MENU_KEYBOARD = "Меню";
    /**
     * Отображение, в которой ключи - команды,
     * значения - список допустимых количеств параметров, передаваемых вместе с командой.
     */
    public static final Map<String, List<Integer>> commandsAndNumberOfParams = Map.of(
            START, List.of(0),
            GENERATE, List.of(2, 0),
            SAVE, List.of(1, 2, 0),
            LIST, List.of(0),
            EDIT, List.of(3, 4, 0),
            DELETE, List.of(1, 0),
            HELP, List.of(0),
            SORT, List.of(0),
            FIND, List.of(1, 0)
    );
    /**
     * Отображение, где кнопки соотносятся с командами
     */
    public static final Map<String, String> commandKeyMapping = Map.of(
            GENERATE_KEYBOARD, GENERATE,
            EDIT_KEYBOARD, EDIT,
            DELETE_KEYBOARD, DELETE,
            SAVE_KEYBOARD, SAVE,
            LIST_KEYBOARD, LIST,
            HELP_KEYBOARD, HELP,
            SORT_KEYBOARD, SORT,
            FIND_KEYBOARD, FIND,
            MENU_KEYBOARD, START
    );

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Command() {
    }
}
