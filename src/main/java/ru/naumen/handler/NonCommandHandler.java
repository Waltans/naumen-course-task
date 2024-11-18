package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.*;

import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер сообщений, не являющихся командой
 */
@Component
public class NonCommandHandler {
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private final HandlerMapper handlerMapper;

    public NonCommandHandler(UserStateCache userStateCache, ValidationService validationService, HandlerMapper handlerMapper) {
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.handlerMapper = handlerMapper;
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
        try {
            validationService.isValidComplexity(complexity);
            userStateCache.addParam(userId, complexity);
            List<String> params = userStateCache.getUserParams(userId);

            userStateCache.setState(userId, nextState);
            if (nextState == NONE) {
                String[] splitCommand = {Command.GENERATE, params.get(0), complexity};

                return handlerMapper.getHandler(Command.GENERATE).handle(splitCommand, userId);
            }

            return new Response(response, nextState);
        } catch (IllegalArgumentException e) {
            userStateCache.setState(userId, currentState);

            return new Response(e.getMessage(), currentState);
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
        try {
            validationService.isValidLength(Integer.parseInt(length));
            userStateCache.setState(userId, nextState);
            userStateCache.addParam(userId, length);

            return new Response(ENTER_PASSWORD_COMPLEXITY, nextState);
        } catch (IllegalArgumentException e) {
            userStateCache.setState(userId, currentState);

            return new Response(e.getMessage(), currentState);
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

        if (currentState.equals(SAVE_STEP_2)) {
            String[] splitCommand = {Command.SAVE, userStateCache.getUserParams(userId).get(0), description};

            return handlerMapper.getHandler(Command.SAVE).handle(splitCommand, userId);
        } else if (currentState.equals(EDIT_STEP_4)) {
            String[] splitCommand = {Command.EDIT,
                    userStateCache.getUserParams(userId).get(0),
                    userStateCache.getUserParams(userId).get(1),
                    userStateCache.getUserParams(userId).get(2),
                    description};

            return handlerMapper.getHandler(Command.EDIT).handle(splitCommand, userId);
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
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, index), NONE);
        }

        if (currentState.equals(EDIT_STEP_1)) {
            userStateCache.setState(userId, EDIT_STEP_2);

            return new Response(ENTER_PASSWORD_LENGTH, EDIT_STEP_2);
        } else if (currentState.equals(DELETE_STEP_1)) {
            String[] splitCommand = new String[]{Command.DELETE, index};

            return handlerMapper.getHandler(Command.DELETE).handle(splitCommand, userId);
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
        if (currentState.equals(SORT_STEP_1)) {
            String[] splitCommand = {sortType};
            return handlerMapper.getHandler(Command.SORT).handle(splitCommand, userId);
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
        if (currentState.equals(FIND_STEP_1)) {
            String[] splitCommand = {Command.FIND, searchRequest};
            return handlerMapper.getHandler(Command.FIND).handle(splitCommand, userId);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, currentState);
    }
}
