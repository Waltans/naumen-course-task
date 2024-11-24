package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Command;

import java.util.HashMap;
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
    private final RemindHandler remindHandler;
    private final AddCodePhraseHandler addCodePhraseHandler;
    private final ClearPasswordHandler clearPasswordHandler;

    public HandlerMapper(DeleteHandler deleteHandler,
                         EditHandler editHandler,
                         FindHandler findHandler,
                         GenerateHandler generateHandler,
                         ListHandler listHandler,
                         SaveHandler saveHandler,
                         SortHandler sortHandler,
                         StartHelpHandler startHelpHandler,
                         RemindHandler remindHandler,
                         AddCodePhraseHandler addCodePhraseHandler,
                         ClearPasswordHandler clearPasswordHandler) {
        this.deleteHandler = deleteHandler;
        this.editHandler = editHandler;
        this.findHandler = findHandler;
        this.generateHandler = generateHandler;
        this.listHandler = listHandler;
        this.saveHandler = saveHandler;
        this.sortHandler = sortHandler;
        this.startHelpHandler = startHelpHandler;
        this.remindHandler = remindHandler;
        this.addCodePhraseHandler = addCodePhraseHandler;
        this.clearPasswordHandler = clearPasswordHandler;
    }

    /**
     * Возвращает хэндлер по команде
     *
     * @param command команда
     */
    public CommandHandler getHandler(String command) {
        Map<String, CommandHandler> commandsAndHandlers = new HashMap<>();
        commandsAndHandlers.put(Command.GENERATE, generateHandler);
        commandsAndHandlers.put(Command.LIST, listHandler);
        commandsAndHandlers.put(Command.SAVE, saveHandler);
        commandsAndHandlers.put(Command.EDIT, editHandler);
        commandsAndHandlers.put(Command.DELETE, deleteHandler);
        commandsAndHandlers.put(Command.SORT, sortHandler);
        commandsAndHandlers.put(Command.FIND, findHandler);
        commandsAndHandlers.put(Command.START, startHelpHandler);
        commandsAndHandlers.put(Command.HELP, startHelpHandler);
        commandsAndHandlers.put(Command.REMIND, remindHandler);
        commandsAndHandlers.put(Command.ADD_CODE, addCodePhraseHandler);
        commandsAndHandlers.put(Command.CLEAR, clearPasswordHandler);

        return commandsAndHandlers.get(command);
    }
}
