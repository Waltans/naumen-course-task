package ru.naumen.bot;

import java.util.HashMap;
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
    public static final String REMIND = "/remind";
    public static final String REMIND_KEYBOARD = "Напомнить";
    public static final String ADD_CODE = "/code";
    public static final String ADD_CODE_KEYBOAD = "Добавить кодовое слово";
    public static final String ADD_CODE_PHRASE = "Введите кодовое слово";
    /**
     * Отображение, в которой ключи - команды,
     * значения - список допустимых количеств параметров, передаваемых вместе с командой.
     */
    public static final Map<String, List<Integer>> commandsAndNumberOfParams;

    static {
        commandsAndNumberOfParams = new HashMap<>();
        commandsAndNumberOfParams.put(START, List.of(0));
        commandsAndNumberOfParams.put(GENERATE, List.of(2, 0));
        commandsAndNumberOfParams.put(SAVE, List.of(1, 2, 3, 0));
        commandsAndNumberOfParams.put(LIST, List.of(0));
        commandsAndNumberOfParams.put(EDIT, List.of(3, 4, 0));
        commandsAndNumberOfParams.put(DELETE, List.of(1, 0));
        commandsAndNumberOfParams.put(HELP, List.of(0));
        commandsAndNumberOfParams.put(SORT, List.of(0));
        commandsAndNumberOfParams.put(FIND, List.of(1, 0));
        commandsAndNumberOfParams.put(REMIND, List.of(2, 0));
        commandsAndNumberOfParams.put(ADD_CODE, List.of(1, 0));
    }

    /**
     * Отображение, где кнопки соотносятся с командами
     */
    public static final Map<String, String> commandKeyMapping;

    static {
        commandKeyMapping = new HashMap<>();
        commandKeyMapping.put(GENERATE_KEYBOARD, GENERATE);
        commandKeyMapping.put(EDIT_KEYBOARD, EDIT);
        commandKeyMapping.put(DELETE_KEYBOARD, DELETE);
        commandKeyMapping.put(SAVE_KEYBOARD, SAVE);
        commandKeyMapping.put(LIST_KEYBOARD, LIST);
        commandKeyMapping.put(HELP_KEYBOARD, HELP);
        commandKeyMapping.put(SORT_KEYBOARD, SORT);
        commandKeyMapping.put(FIND_KEYBOARD, FIND);
        commandKeyMapping.put(REMIND_KEYBOARD, REMIND);
        commandKeyMapping.put(MENU_KEYBOARD, START);
        commandKeyMapping.put(ADD_CODE_KEYBOAD, ADD_CODE);
    }

    /**
     * Приватный конструктор, чтобы нельзя было создавать объекты
     */
    private Command() {
    }
}
