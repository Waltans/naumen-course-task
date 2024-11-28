package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.ValidationService;

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
        if (validationService.isValidComplexity(complexity)) {
            userStateCache.addParam(userId, complexity);
            List<String> params = userStateCache.getUserParams(userId);

            userStateCache.setState(userId, nextState);
            if (nextState == NONE) {
                String[] splitCommand = {Command.GENERATE, params.get(0), complexity};

                return handlerMapper.getHandler(Command.GENERATE).handle(splitCommand, userId);
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

        if (currentState.equals(SAVE_STEP_2)) {
            userStateCache.addParam(userId, description);
            userStateCache.setState(userId, nextState);

            return new Response(ENTER_REMIND_DAYS_ON_SAVE, nextState);
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

        if (currentState.equals(REMIND_STEP_1)) {
            userStateCache.setState(userId, REMIND_STEP_2);

            return new Response(ENTER_REMIND_DAYS, REMIND_STEP_2);
        } else if (currentState.equals(EDIT_STEP_1)) {
            userStateCache.setState(userId, EDIT_STEP_2);

            return new Response(ENTER_PASSWORD_LENGTH, EDIT_STEP_2);
        } else if (currentState.equals(DELETE_STEP_1)) {
            String[] splitCommand = new String[]{Command.DELETE, index};

            return handlerMapper.getHandler(Command.DELETE).handle(splitCommand, userId);
        }

        return new Response(ENTER_PASSWORD_LENGTH, currentState);
    }

    /**
     * Получение дней до напоминания
     *
     * @param daysToRemind - число дней до напоминания
     * @param userId       - ID пользователя
     * @param nextState    - следующее состояние
     * @return ответ и состояние пользователя
     */
    public Response getRemindDays(String daysToRemind, long userId, State nextState) {
        State currentState = userStateCache.getUserState(userId);
        if (daysToRemind.equals("0") &&
                currentState.equals(SAVE_STEP_3)) {
            userStateCache.setState(userId, nextState);
            String[] splitCommand = new String[]{Command.SAVE,
                    userStateCache.getUserParams(userId).get(0),
                    userStateCache.getUserParams(userId).get(1)};

            return handlerMapper.getHandler(Command.SAVE).handle(splitCommand, userId);
        }

        if (!validationService.isValidDays(Integer.parseInt(daysToRemind))) {
            userStateCache.setState(userId, currentState);
            return new Response(DAYS_ERROR_MESSAGE, currentState);
        }

        userStateCache.setState(userId, nextState);
        userStateCache.addParam(userId, daysToRemind);
        if (currentState.equals(SAVE_STEP_3)) {
            String[] splitCommand = new String[]{Command.SAVE,
                    userStateCache.getUserParams(userId).get(0),
                    userStateCache.getUserParams(userId).get(1),
                    daysToRemind};

            return handlerMapper.getHandler(Command.SAVE).handle(splitCommand, userId);
        }
        String[] splitCommand = new String[]{Command.REMIND,
                userStateCache.getUserParams(userId).get(0), daysToRemind};

        return handlerMapper.getHandler(Command.REMIND).handle(splitCommand, userId);
    }

    /**
     * Метод для получения кодового слова
     *
     * @param codeWord - кодовое слово
     * @param userId   - ID пользователя
     * @return - сообщение и состояние пользователя
     */
    public Response getCodeWord(String codeWord, long userId){
        State currentState = userStateCache.getUserState(userId);
        if (currentState.equals(CODE_PHRASE_1)) {
            return handlerMapper.getHandler(Command.ADD_CODE)
                    .handle(new String[]{Command.ADD_CODE, codeWord}, userId);
        }

        if (currentState.equals(CLEAR_1)) {
            userStateCache.addParam(userId, codeWord);
            userStateCache.setState(userId, CLEAR_2);

            return new Response(ENTER_CLEAR_PASSWORD, CLEAR_2);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, currentState);
    }


    /**
     * Получение типа сортировки из команды
     *
     * @param sortType тип сортировки
     * @param userId   id пользователя
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
     *
     * @param searchRequest поисковый запрос
     * @param userId        id пользователя
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

    /**
     * Получить слово для очистки паролей
     *
     * @param phrase - слово, которое является началом описаний паролей и мы хотим удалить
     * @param userId - ID пользователя
     * @return - результат очистки паролей
     */
    public Response getPhraseForClear(String phrase, long userId) {
        State currentState = userStateCache.getUserState(userId);
        if (currentState.equals(CLEAR_2)) {
            List<String> userParams = userStateCache.getUserParams(userId);
            return handlerMapper.getHandler(Command.CLEAR)
                    .handle(new String[]{Command.CLEAR, userParams.getFirst(), phrase},
                            userId);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, currentState);
    }
}
