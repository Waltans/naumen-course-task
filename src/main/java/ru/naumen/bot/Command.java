package ru.naumen.bot;

import java.util.List;
import java.util.Map;

/**
 * Команды как константы и количество допустимых параметров для каждой команды
 */
public class Command {
    public static final String GENERATE = "/generate";
    public static final String EDIT = "/edit";
    public static final String DELETE = "/delete";
    public static final String SAVE = "/save";
    public static final String LIST = "/list";
    public static final String HELP = "/help";
    public static final String START = "/start";
    /**
     * Отображение, в которой ключи - команды, 
     * значения - список допустимых количеств параметров, передаваемых вместе с командой.
     */
    public static final Map<String, List<Integer>> commandsAndNumberOfParams = Map.of(
            START, List.of(0),
            GENERATE, List.of(2),
            SAVE, List.of(1, 2),
            LIST, List.of(0),
            EDIT, List.of(3, 4),
            DELETE, List.of(1),
            HELP, List.of(0)
    );

    private Command() {
    }
}
