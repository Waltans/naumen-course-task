package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.command.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.*;

import java.util.List;
import java.util.Map;

import static ru.naumen.bot.constants.Errors.*;
import static ru.naumen.bot.constants.Requests.*;

/**
 * Хэндлер сообщений, не являющихся командой
 */
@Component
public class NonCommandHandler {
    private final UserStateCache userStateCache;
    private final ValidationService validationService;

    /**
     * Хэндлеры команд
     * Название бина (сама команда формата "/command") -> хэндлер
     */
    private final Map<String, CommandHandler> commandHandlers;


    public NonCommandHandler(UserStateCache userStateCache,
                             ValidationService validationService,
                             Map<String, CommandHandler> commandHandlers) {
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.commandHandlers = commandHandlers;
    }

    /**
     * Получает сложность пароля
     *
     * @param complexity - команда, содержащая сложность пароля
     * @param userId     - ID пользователя
     * @param nextState  - следующее состояние
     * @param response   - ответ в случае завершения
     * @return ответ и состояние пользователя
     */
    public Response getComplexity(String complexity, long userId, State nextState, String response) {
        State currentState = userStateCache.getUserState(userId);
        if (validationService.isValidComplexity(complexity)) {
            userStateCache.addParam(userId, complexity);
            List<String> params = userStateCache.getUserParams(userId);

            userStateCache.setState(userId, nextState);
            if (nextState == State.NONE) {
                String[] splitCommand = {Command.GENERATE.getCommand(), params.get(0), complexity};

                CommandHandler handler = commandHandlers.get(Command.GENERATE.getCommand());
                return handler.handle(splitCommand, userId);
            }

            return new Response(response, nextState);
        } else {
            userStateCache.setState(userId, currentState);
            return new Response(COMPLEXITY_ERROR_MESSAGE, currentState);
        }
    }

    /**
     * Получение длины пароля
     *
     * @param length    - сообщение содержащее длину
     * @param userId    - ID пользователя
     * @param nextState - следующее состояние
     * @return ответ и состояние пользователя
     */
    public Response getPasswordLength(String length, long userId, State nextState) {
        State currentState = userStateCache.getUserState(userId);
        if (validationService.isValidLength(Integer.parseInt(length))) {
            userStateCache.setState(userId, nextState);
            userStateCache.addParam(userId, length);

            return new Response(ENTER_PASSWORD_COMPLEXITY, nextState);
        } else {
            userStateCache.setState(userId, currentState);
            return new Response(LENGTH_ERROR_MESSAGE, currentState);
        }
    }

    /**
     * Получает описание пароля из команды
     *
     * @param description - входящая команда
     * @param userId      -ID пользователя
     * @param nextState   - следующее состояние
     * @param response    - ответ в случае завершения
     * @return ответ и состояние пользователя
     */
    public Response getDescription(String description, long userId, State nextState, String response) {
        userStateCache.addParam(userId, description);
        State currentState = userStateCache.getUserState(userId);
        userStateCache.setState(userId, nextState);

        if (currentState.equals(State.SAVE_STEP_2)) {
            String[] splitCommand = {Command.SAVE.getCommand(), userStateCache.getUserParams(userId).get(0), description};

            CommandHandler handler = commandHandlers.get(Command.SAVE.getCommand());
            return handler.handle(splitCommand, userId);
        } else if (currentState.equals(State.EDIT_STEP_4)) {
            String[] splitCommand = {Command.EDIT.getCommand(),
                    userStateCache.getUserParams(userId).get(0),
                    userStateCache.getUserParams(userId).get(1),
                    userStateCache.getUserParams(userId).get(2),
                    description};

            CommandHandler handler = commandHandlers.get(Command.EDIT.getCommand());
            return handler.handle(splitCommand, userId);
        }

        return new Response(response, currentState);
    }

    /**
     * Получение пароля
     *
     * @param password  - пароль пользователя
     * @param userId    - ID пользователя
     * @param nextState - следующее состояние пользователя
     * @return ответ и состояние пользователя
     */
    public Response getPassword(String password, long userId, State nextState) {
        userStateCache.addParam(userId, password);
        userStateCache.setState(userId, nextState);

        return new Response(ENTER_PASSWORD_DESCRIPTION, nextState);
    }

    /**
     * Получение индекса из команды
     *
     * @param index  - пришедшее сообщение
     * @param userId - ID пользователя
     * @return ответ и состояние пользователя
     */
    public Response getIndexPassword(String index, long userId) {
        userStateCache.addParam(userId, index);
        State currentState = userStateCache.getUserState(userId);

        if (!validationService.isValidPasswordIndex(userId, Integer.parseInt(index))) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, index), State.NONE);
        }

        if (currentState.equals(State.EDIT_STEP_1)) {
            userStateCache.setState(userId, State.EDIT_STEP_2);

            return new Response(ENTER_PASSWORD_LENGTH, State.EDIT_STEP_2);
        } else if (currentState.equals(State.DELETE_STEP_1)) {
            String[] splitCommand = new String[]{Command.DELETE.getCommand(), index};

            CommandHandler handler = commandHandlers.get(Command.DELETE.getCommand());
            return handler.handle(splitCommand, userId);
        }

        return new Response(ENTER_PASSWORD_LENGTH, currentState);
    }

    /**
     * Получение типа сортировки из команды
     * @param sortType тип сортировки
     * @param userId id пользователя
     * @return ответ и состояние пользователя
     */
    public Response getSortType(String sortType, Long userId) {
        State currentState = userStateCache.getUserState(userId);
        if (currentState.equals(State.SORT_STEP_1)) {
            String[] splitCommand = {sortType};
            CommandHandler handler = commandHandlers.get(Command.SORT.getCommand());
            return handler.handle(splitCommand, userId);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, currentState);
    }

    /**
     * Получение поискового запроса из команды
     * @param searchRequest поисковый запрос
     * @param userId id пользователя
     * @return ответ и состояние пользователя
     */
    public Response getSearchRequest(String searchRequest, Long userId) {
        State currentState = userStateCache.getUserState(userId);
        if (currentState.equals(State.FIND_STEP_1)) {
            String[] splitCommand = {Command.FIND.getCommand(), searchRequest};
            CommandHandler handler = commandHandlers.get(Command.FIND.getCommand());
            return handler.handle(splitCommand, userId);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, currentState);
    }
}
