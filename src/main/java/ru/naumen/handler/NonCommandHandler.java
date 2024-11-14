package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.*;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.bot.Constants.ENTER_PASSWORD_LENGTH;
import static ru.naumen.model.State.*;

/**
 * Хэндлер сообщений, не являющихся командой
 */
@Component
public class NonCommandHandler {
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private final GenerateHandler generateHandler;
    private final EditHandler editHandler;
    private final DeleteHandler deleteHandler;
    private final SaveHandler saveHandler;
    private final SortHandler sortHandler;
    private final FindHandler findHandler;

    public NonCommandHandler(UserStateCache userStateCache, ValidationService validationService, GenerateHandler generateHandler, EditHandler editHandler, DeleteHandler deleteHandler, SaveHandler saveHandler, SortHandler sortHandler, FindHandler findHandler) {
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.generateHandler = generateHandler;
        this.editHandler = editHandler;
        this.deleteHandler = deleteHandler;
        this.saveHandler = saveHandler;
        this.sortHandler = sortHandler;
        this.findHandler = findHandler;
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
        State currentState = userStateCache.getTotalUserState().get(userId);
        try {
            validationService.isValidComplexity(Integer.parseInt(complexity));
            List<String> params = userStateCache.getTotalUserParams().get(userId);
            params.add(complexity);
            userStateCache.getTotalUserState().put(userId, nextState);
            if (nextState == NONE) {
                String[] splitCommand = {Command.EDIT, params.get(0), complexity};

                return generateHandler.generatePassword(splitCommand, userId);
            }

            return new Response(response, nextState);
        } catch (IllegalArgumentException e) {
            userStateCache.getTotalUserState().put(userId, currentState);

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
        State currentState = userStateCache.getTotalUserState().get(userId);
        try {
            validationService.isValidLength(Integer.parseInt(length));
            userStateCache.getTotalUserState().put(userId, nextState);
            userStateCache.getTotalUserParams().get(userId).add(length);

            return new Response(ENTER_PASSWORD_COMPLEXITY, nextState);
        } catch (IllegalArgumentException e) {
            userStateCache.getTotalUserState().put(userId, currentState);

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
        List<String> params = userStateCache.getTotalUserParams().get(userId);
        params.add(description);
        State currentState = userStateCache.getTotalUserState().get(userId);
        userStateCache.getTotalUserState().put(userId, nextState);

        if (currentState.equals(SAVE_STEP_2)) {
            String[] splitCommand = {Command.SAVE, params.get(0), description};

            return saveHandler.savePassword(splitCommand, userId);
        } else if (currentState.equals(EDIT_STEP_4)) {
            String[] splitCommand = {Command.EDIT, params.get(0), params.get(1), params.get(2), description};

            return editHandler.updatePassword(splitCommand, userId);
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
        userStateCache.getTotalUserParams().get(userId).add(password);
        userStateCache.getTotalUserState().put(userId, nextState);

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
        userStateCache.getTotalUserParams().get(userId).add(index);
        State currentState = userStateCache.getTotalUserState().get(userId);

        if (!validationService.isValidPasswordIndex(userId, Integer.parseInt(index))) {
            userStateCache.getTotalUserState().put(userId, NONE);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, index), NONE);
        }

        if (currentState.equals(EDIT_STEP_1)) {
            userStateCache.getTotalUserState().put(userId, EDIT_STEP_2);

            return new Response(ENTER_PASSWORD_LENGTH, EDIT_STEP_2);
        } else if (currentState.equals(DELETE_STEP_1)) {
            String[] splitCommand = new String[]{Command.DELETE, index};

            return deleteHandler.deletePassword(splitCommand, userId);
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
        userStateCache.getTotalUserParams().get(userId).add(sortType);
        State currentState = userStateCache.getTotalUserState().get(userId);
        if (currentState.equals(SORT_STEP_1)) {
            String[] splitCommand = {sortType};
            return sortHandler.sortPasswords(splitCommand, userId);
        }

        return new Response(FAILURE, currentState);
    }

    /**
     * Получение поискового запроса из команды
     * @param searchRequest поисковый запрос
     * @param userId id пользователя
     * @return ответ и состояние пользователя
     */
    public Response getSearchRequest(String searchRequest, Long userId) {
        userStateCache.getTotalUserParams().get(userId).add(searchRequest);
        State currentState = userStateCache.getTotalUserState().get(userId);
        if (currentState.equals(FIND_STEP_1)) {
            String[] splitCommand = {Command.FIND, searchRequest};
            return findHandler.findPasswords(splitCommand, userId);
        }

        return new Response(FAILURE, currentState);
    }
}
