package ru.naumen.service;

import org.springframework.stereotype.Service;
import ru.naumen.bot.Response;
import ru.naumen.bot.command.Command;
import ru.naumen.handler.CommandHandler;
import ru.naumen.handler.NonCommandHandler;
import ru.naumen.model.State;
import ru.naumen.repository.UserStateCache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Parameters.BY_DATE;
import static ru.naumen.bot.constants.Parameters.BY_DESCRIPTION;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_DESCRIPTION;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {
    private final UserStateCache userStateCache;
    private final NonCommandHandler nonCommandHandler;

    /**
     * Хэндлеры команд
     * Название бина (сама команда формата "/command") -> хэндлер
     */
    private final Map<String, CommandHandler> commandHandlers;

    public CommandService(UserStateCache userStateCache,
                          NonCommandHandler nonCommandHandler,
                          Map<String, CommandHandler> commandHandlers) {
        this.userStateCache = userStateCache;
        this.nonCommandHandler = nonCommandHandler;
        this.commandHandlers = commandHandlers;
    }

    /**
     * Обрабатывает команду, введённую пользователем
     *
     * @param message текст команды
     * @param userId  ID пользователя
     * @return ответ на команду и состояние пользователя
     */
    public Response performCommand(String message, long userId) {
        String[] splitCommand = message.split(" ");

        if (!isValidCommand(splitCommand, userStateCache.getUserState(userId))) {
            return new Response(INCORRECT_COMMAND_RESPONSE);
        }

        return doCommand(userId, splitCommand);
    }

    /**
     * Проверяет корректность команды
     *
     * @param splitCommand разделённая по пробелам команда
     * @param state        - ID пользователя
     * @return true, если команда и её параметры корректны, иначе false
     */
    private boolean isValidCommand(String[] splitCommand, State state) {
        String commandString = splitCommand[0];
        int paramsCount = splitCommand.length - 1;

        if (state != null && !state.equals(State.NONE) && !state.equals(State.IN_LIST)) {
            return switch (state) {
                case SAVE_STEP_1, SAVE_STEP_2, EDIT_STEP_4, FIND_STEP_1, GENERATION_STEP_2, EDIT_STEP_3 -> true;
                case GENERATION_STEP_1, EDIT_STEP_1, EDIT_STEP_2, DELETE_STEP_1 -> isNumber(commandString);
                case SORT_STEP_1 -> isValidSortType(commandString);
                default -> false;
            };
        }

        List<Integer> params;

        Optional<Command> command = Command.getCommand(commandString);
        if (command.isPresent()) {
            params = command.get().getValidParamCounts();
        } else {
            params = List.of();
        }

        return params != null
                && params.contains(paramsCount);
    }

    /**
     * Проверяет, является ли валидным тип сортировки
     *
     * @param sortType тип сортировки
     * @return true, если тип введен корректно
     */
    private boolean isValidSortType(String sortType) {
        return sortType.equals(BY_DATE)
                || sortType.equals(BY_DESCRIPTION);
    }

    /**
     * Проверяет, является ли строка числом
     *
     * @param string строка
     * @return true, если строка состоит из числа
     */
    private boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Метод принимает команду и исполняет её
     *
     * @param userId       - ID пользователя
     * @param splitCommand - разделенная команда
     * @return - результат обработки команды
     */
    private Response doCommand(long userId, String[] splitCommand) {
        Optional<Command> command = Command.getCommand(splitCommand[0]);
        if (command.isEmpty()) {
            return performNotCommandMessage(splitCommand, userId);
        }
        CommandHandler handler = commandHandlers.get(command.get().getCommand());
        return handler.handle(splitCommand, userId);
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
            return new Response(INCORRECT_COMMAND_RESPONSE);
        }
        final String command = splitCommand[0];
        return switch (userStateCache.getUserState(userId)) {
            case GENERATION_STEP_1 -> nonCommandHandler.getPasswordLength(command, userId, State.GENERATION_STEP_2);
            case GENERATION_STEP_2 -> nonCommandHandler.getComplexity(command, userId, State.NONE, null);
            case SAVE_STEP_1 -> nonCommandHandler.getPassword(command, userId, State.SAVE_STEP_2);
            case SAVE_STEP_2, EDIT_STEP_4 -> nonCommandHandler.getDescription(command, userId, State.NONE, null);
            case EDIT_STEP_1, DELETE_STEP_1 -> nonCommandHandler.getIndexPassword(command, userId);
            case EDIT_STEP_2 -> nonCommandHandler.getPasswordLength(command, userId, State.EDIT_STEP_3);
            case EDIT_STEP_3 ->
                    nonCommandHandler.getComplexity(command, userId, State.EDIT_STEP_4, ENTER_PASSWORD_DESCRIPTION);
            case SORT_STEP_1 -> nonCommandHandler.getSortType(command, userId);
            case FIND_STEP_1 -> nonCommandHandler.getSearchRequest(command, userId);
            default -> new Response(INCORRECT_COMMAND_RESPONSE);
        };
    }
}
