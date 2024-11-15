package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Command;

import java.util.Map;

/**
 * Маппер хэндлеров
 */
@Component
public class HandlerMapper {

    private final DeleteHandler deleteHandler;
    private final EditHandler editHandler;
    private final FindHandler findHandler;
    private final GenerateHandler generateHandler;
    private final ListHandler listHandler;
    private final SaveHandler saveHandler;
    private final SortHandler sortHandler;
    private final StartHelpHandler startHelpHandler;

    public HandlerMapper(DeleteHandler deleteHandler,
                         EditHandler editHandler,
                         FindHandler findHandler,
                         GenerateHandler generateHandler,
                         ListHandler listHandler,
                         SaveHandler saveHandler,
                         SortHandler sortHandler,
                         StartHelpHandler startHelpHandler) {
        this.deleteHandler = deleteHandler;
        this.editHandler = editHandler;
        this.findHandler = findHandler;
        this.generateHandler = generateHandler;
        this.listHandler = listHandler;
        this.saveHandler = saveHandler;
        this.sortHandler = sortHandler;
        this.startHelpHandler = startHelpHandler;
    }

    /**
     * Возвращает хэндлер по команде
     * @param command команда
     */
    public CommandHandler getHandler(String command) {
        Map<String, CommandHandler> commandsAndHandlers = Map.of(
                Command.GENERATE, generateHandler,
                Command.LIST, listHandler,
                Command.SAVE, saveHandler,
                Command.EDIT, editHandler,
                Command.DELETE, deleteHandler,
                Command.SORT, sortHandler,
                Command.FIND, findHandler,
                Command.START, startHelpHandler,
                Command.HELP, startHelpHandler
        );

        return commandsAndHandlers.get(command);
    }
}
