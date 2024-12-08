package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.command.Command;
import ru.naumen.cache.UserStateCache;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;
import ru.naumen.service.PasswordService;

import java.util.List;
import java.util.Map;

import static ru.naumen.bot.constants.Errors.INDEX_ERROR_MESSAGE;
import static ru.naumen.bot.constants.Errors.PASSWORD_NOT_FOUND_MESSAGE;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_DESCRIPTION;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_LENGTH;

/**
 * Хэндлер сообщений, не являющихся командой
 */
@Component
public class NonCommandHandler {
    private final UserStateCache userStateCache;
    private final PasswordService passwordService;

    /**
     * Ответ с запросом на выбор сложности пароля
     */
    private static final String ENTER_PASSWORD_COMPLEXITY_REQUEST = "Выберите сложность пароля";

    /**
     * Ответ с ошибкой
     */
    private static final String FAILURE = "Что-то пошло не так :( ";

    /**
     * Хэндлеры команд
     * Название бина (сама команда формата "/command") -> хэндлер
     */
    private final Map<String, CommandHandler> commandHandlers;
    private final KeyboardCreator keyboardCreator;


    public NonCommandHandler(UserStateCache userStateCache,
                             PasswordService passwordService,
                             Map<String, CommandHandler> commandHandlers,
                             KeyboardCreator keyboardCreator) {
        this.userStateCache = userStateCache;
        this.passwordService = passwordService;
        this.commandHandlers = commandHandlers;
        this.keyboardCreator = keyboardCreator;
    }

    /**
     * Получает сложность пароля
     *
     * @param complexity - команда, содержащая сложность пароля
     * @param userId     - ID пользователя
     * @param nextState  - следующее состояние
     * @param response   - ответ в случае завершения
     */
    public Response getComplexity(String complexity, long userId,
                                  State nextState, String response) {
        userStateCache.addParam(userId, complexity);
        List<String> params = userStateCache.getUserParams(userId);

        userStateCache.setState(userId, nextState);
        if (nextState == State.NONE) {
            String[] splitCommand = {Command.GENERATE.getCommand(), params.get(0), complexity};

            CommandHandler handler = commandHandlers.get(Command.GENERATE.getCommand());
            return handler.handle(splitCommand, userId);
        }

        return new Response(response, keyboardCreator.createEmptyKeyboard());
    }

    /**
     * Получение длины пароля
     *
     * @param length    - сообщение содержащее длину
     * @param userId    - ID пользователя
     * @param nextState - следующее состояние
     */
    public Response getPasswordLength(String length, long userId, State nextState) {
        userStateCache.setState(userId, nextState);
        userStateCache.addParam(userId, length);

        return new Response(ENTER_PASSWORD_COMPLEXITY_REQUEST, keyboardCreator.createSelectComplexityKeyboard());
    }

    /**
     * Получает описание пароля из команды
     *
     * @param description - входящая команда
     * @param userId      -ID пользователя
     * @param nextState   - следующее состояние
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

        return new Response(response, keyboardCreator.createEmptyKeyboard());
    }

    /**
     * Получение пароля
     *
     * @param password  - пароль пользователя
     * @param userId    - ID пользователя
     * @param nextState - следующее состояние пользователя
     */
    public Response getPassword(String password, long userId, State nextState) {
        userStateCache.addParam(userId, password);
        userStateCache.setState(userId, nextState);

        return new Response(ENTER_PASSWORD_DESCRIPTION, keyboardCreator.createEmptyKeyboard());
    }

    /**
     * Получение индекса из команды
     *
     * @param index  - пришедшее сообщение
     * @param userId - ID пользователя
     */
    public Response getIndexPassword(String index, long userId) {
        userStateCache.addParam(userId, index);
        State currentState = userStateCache.getUserState(userId);

        try {
            if (!passwordService.isValidPasswordIndex(Integer.parseInt(index), userId)) {
                userStateCache.setState(userId, State.IN_LIST);
                userStateCache.clearParamsForUser(userId);

                return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, index), keyboardCreator.createInListKeyboard());
            }
        } catch (NumberFormatException e) {
            userStateCache.setState(userId, State.IN_LIST);
            return new Response(INDEX_ERROR_MESSAGE, keyboardCreator.createInListKeyboard());
        }

        if (currentState.equals(REMIND_STEP_1)) {
            userStateCache.setState(userId, REMIND_STEP_2);

            return new Response(ENTER_REMIND_DAYS, REMIND_STEP_2);
        } else if (currentState.equals(State.EDIT_STEP_1)) {
            userStateCache.setState(userId, State.EDIT_STEP_2);

            return new Response(ENTER_PASSWORD_LENGTH, keyboardCreator.createEmptyKeyboard());
        } else if (currentState.equals(State.DELETE_STEP_1)) {
            String[] splitCommand = new String[]{Command.DELETE.getCommand(), index};

            CommandHandler handler = commandHandlers.get(Command.DELETE.getCommand());
            return handler.handle(splitCommand, userId);
        }

        return new Response(ENTER_PASSWORD_LENGTH, keyboardCreator.createEmptyKeyboard());
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
                currentState.equals(SAVE_STEP_4)) {
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
        if (currentState.equals(SAVE_STEP_4)) {
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
    public Response getCodeWord(String codeWord, long userId) {
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
     */
    public Response getSortType(String sortType, Long userId) {
        State currentState = userStateCache.getUserState(userId);
        if (currentState.equals(State.SORT_STEP_1)) {
            String[] splitCommand = {sortType};
            CommandHandler handler = commandHandlers.get(Command.SORT.getCommand());
            return handler.handle(splitCommand, userId);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, keyboardCreator.createMainKeyboard());
    }

    /**
     * Получение поискового запроса из команды
     *
     * @param searchRequest поисковый запрос
     * @param userId        id пользователя
     */
    public Response getSearchRequest(String searchRequest, Long userId) {
        State currentState = userStateCache.getUserState(userId);
        if (currentState.equals(State.FIND_STEP_1)) {
            String[] splitCommand = {Command.FIND.getCommand(), searchRequest};
            CommandHandler handler = commandHandlers.get(Command.FIND.getCommand());
            return handler.handle(splitCommand, userId);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, keyboardCreator.createMainKeyboard());
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
            userStateCache.setState(userId, CLEAR_3);
            userStateCache.addParam(userId, phrase);
            int userPasswordsSize = passwordService.findCountPasswordsStartedFrom(userId, phrase);
            String matchForm = getMatchForm(userPasswordsSize);
            if (phrase.equalsIgnoreCase(ALL)) {
                userPasswordsSize = passwordService.countPasswordsByUserId(userId);
                matchForm = getMatchForm(userPasswordsSize);
                return new Response(String.format(ENTER_AGREEMENT_ALL, userPasswordsSize, matchForm), CLEAR_3);
            }

            return new Response(String.format(ENTER_AGREEMENT, userPasswordsSize, matchForm, phrase), CLEAR_3);
        }

        userStateCache.clearParamsForUser(userId);
        return new Response(FAILURE, currentState);
    }

    /**
     * Метод получения согласия на отчистку паролей
     *
     * @param agreement - согласие
     * @param userId    - ID пользователя
     * @return Ответ об отчистке
     */
    public Response getAgreement(String agreement, long userId) {
        State currentState = userStateCache.getUserState(userId);

        if (currentState.equals(CLEAR_3) && agreement.equalsIgnoreCase("да")) {
            List<String> userParams = userStateCache.getUserParams(userId);
            return handlerMapper.getHandler(Command.CLEAR)
                    .handle(new String[]{Command.CLEAR, userParams.get(0), userParams.get(1)}, userId);
        } else if (currentState.equals(SAVE_STEP_3) && agreement.equalsIgnoreCase("да")) {
            List<String> userParams = userStateCache.getUserParams(userId);
            return handlerMapper.getHandler(Command.SAVE)
                    .handle(new String[]{Command.SAVE, userParams.get(0), userParams.get(1), "30"}, userId);
        } else {
            if (currentState.equals(SAVE_STEP_3)) {
                userStateCache.setState(userId, SAVE_STEP_4);
                return new Response(ENTER_REMIND_DAYS_ON_SAVE_NOT_AGREE, SAVE_STEP_4);
            }
            userStateCache.clearParamsForUser(userId);
            userStateCache.setState(userId, NONE);

            return new Response(DONT_AGREE, NONE);
        }
    }

    /**
     * Получить форму слова "пароля"
     *
     * @param countDeletedPassword - количество паролей для удаления
     * @return - форму слова "пароль"
     */
    private String getMatchForm(int countDeletedPassword) {
        int preLastDigit = countDeletedPassword % 100 / 10;
        String passwordForm = "совпадение";
        if (preLastDigit == 1) {
            return passwordForm;
        }

        return switch (countDeletedPassword % 10) {
            case 1 -> "совпадение";
            case 2, 3, 4 -> "совпадения";
            default -> "совпадений";
        };
    }
}
