package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.keyboards.KeyboardCreator;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.PASSWORD_LIST_FORMAT;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;

/**
 * Хэндлер поиска паролей
 */
@Component("/find")
public class FindHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final EncodeService encodeService;
    private final KeyboardCreator keyboardCreator;

    /**
     * Сообщение с запросом на ввод поискового запроса
     */
    private static final String ENTER_SEARCH_REQUEST = "Введите поисковый запрос";

    /**
     * Сообщение, когда пароли не найдены
     */
    private static final String NO_PASSWORDS_FOUND = "Не найдены пароли по вашему запросу";

    /**
     * Количество параметров команды
     */
    private static final int PARAMS_COUNT = 1;

    public FindHandler(PasswordService passwordService,
                       UserStateCache userStateCache,
                       EncodeService encodeService,
                       KeyboardCreator keyboardCreator) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.encodeService = encodeService;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.FIND_STEP_1);
            return new Response(ENTER_SEARCH_REQUEST, keyboardCreator.createEmptyKeyboard());
        }

        if (!isValidCommand(splitCommand)) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createEmptyKeyboard());
        }

        String searchRequest = splitCommand[1];
        List<UserPassword> foundPasswords = passwordService.getUserPasswordsWithPartialDescription(userId, searchRequest);

        if (foundPasswords.isEmpty()) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(NO_PASSWORDS_FOUND, keyboardCreator.createMainKeyboard());
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < foundPasswords.size(); i++) {
            String description = foundPasswords.get(i).getDescription();
            String password = encodeService.decryptData(foundPasswords.get(i).getPassword());
            stringBuilder.append(String.format("\n" + PASSWORD_LIST_FORMAT, i + 1, description, password));
        }

        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(stringBuilder.toString(), keyboardCreator.createMainKeyboard());
    }

    /**
     * Валидирует команду
     *
     * @param splitCommand команда, разделённая по пробелам
     * @return true, если команда валидна
     */
    private boolean isValidCommand(String[] splitCommand) {
        return (splitCommand.length - COMMAND_WITHOUT_PARAMS_LENGTH) == PARAMS_COUNT;
    }
}
