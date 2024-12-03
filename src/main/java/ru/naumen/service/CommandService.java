package ru.naumen.service;

import org.springframework.stereotype.Service;
import ru.naumen.bot.command.Command;
import ru.naumen.bot.command.CommandFinder;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.CommandNotFoundException;
import ru.naumen.handler.*;
import ru.naumen.model.State;

import java.util.Map;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_DESCRIPTION;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private final NonCommandHandler nonCommandHandler;
    private final CommandFinder commandFinder;

    /**
     * Хэндлеры команд
     * Название бина (сама команда формата "/command") -> хэндлер
     */
    private final Map<String, CommandHandler> commandHandlers;

    public CommandService(UserStateCache userStateCache,
                          ValidationService validationService,
                          NonCommandHandler nonCommandHandler,
                          CommandFinder commandFinder,
                          Map<String, CommandHandler> commandHandlers) {
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.nonCommandHandler = nonCommandHandler;
        this.commandFinder = commandFinder;
        this.commandHandlers = commandHandlers;
    }

    /**
     * Обрабатывает команду, введённую пользователем
     *
     * @param message  текст команды
     * @param userId   ID пользователя
     * @return ответ на команду и состояние пользователя
     */
    public Response performCommand(String message, long userId) {
        String[] splitCommand = message.split(" ");

        if (!validationService.isValidCommand(splitCommand, userId)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, userStateCache.getUserState(userId));
        }

        return doCommand(userId, splitCommand);
    }

    /**
     * Метод принимает команду и исполняет её
     * @param userId - ID пользователя
     * @param splitCommand - разделенная команда
     * @return - результат обработки команды
     */
    private Response doCommand(long userId, String[] splitCommand) {
        try {
            Command command = commandFinder.findCommand(splitCommand[0]);
            CommandHandler handler = commandHandlers.get(command.getCommand());
            return handler.handle(splitCommand, userId);
        } catch (CommandNotFoundException e) {
            return performNotCommandMessage(splitCommand, userId);
        }
    }

    /**
     * Обработка сообщение, которое не является командой
     *
     * @param splitCommand - входящее сообщение разделенное пробелами
     * @param userId       - ID пользователя
     * @return Состояние пользователя и ответ
     */
    private Response performNotCommandMessage(String[] splitCommand, long userId) {
        if (splitCommand.length > 1) {
            return new Response(INCORRECT_COMMAND_RESPONSE, State.NONE);
        }
        final String command = splitCommand[0];
        return switch (userStateCache.getUserState(userId)) {
            case GENERATION_STEP_1 -> nonCommandHandler.getPasswordLength(command, userId, State.GENERATION_STEP_2);
            case GENERATION_STEP_2 -> nonCommandHandler.getComplexity(command, userId, State.NONE, null);
            case SAVE_STEP_1 -> nonCommandHandler.getPassword(command, userId, State.SAVE_STEP_2);
            case SAVE_STEP_2, EDIT_STEP_4 -> nonCommandHandler.getDescription(command, userId, State.NONE, null);
            case EDIT_STEP_1, DELETE_STEP_1 -> nonCommandHandler.getIndexPassword(command, userId);
            case EDIT_STEP_2 -> nonCommandHandler.getPasswordLength(command, userId, State.EDIT_STEP_3);
            case EDIT_STEP_3 -> nonCommandHandler.getComplexity(command, userId, State.EDIT_STEP_4, ENTER_PASSWORD_DESCRIPTION);
            case SORT_STEP_1 -> nonCommandHandler.getSortType(command, userId);
            case FIND_STEP_1 -> nonCommandHandler.getSearchRequest(command, userId);
            default -> new Response(INCORRECT_COMMAND_RESPONSE, State.NONE);
        };
    }
}
