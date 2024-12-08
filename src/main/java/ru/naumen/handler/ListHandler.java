package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Errors.NO_PASSWORDS_MESSAGE;
import static ru.naumen.bot.constants.Information.PASSWORD_LIST_FORMAT;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;

/**
 * Хэндлер получения списка паролей
 */
@Component("/list")
public class ListHandler implements CommandHandler {
    private final EncodeService encodeService;
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final KeyboardCreator keyboardCreator;

    public ListHandler(EncodeService encodeService, PasswordService passwordService, UserStateCache userStateCache, KeyboardCreator keyboardCreator) {
        this.encodeService = encodeService;
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (!isValidCommand(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createEmptyKeyboard());
        }

        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (userPasswords.isEmpty()) {
            return new Response(NO_PASSWORDS_MESSAGE, keyboardCreator.createMainKeyboard());
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userPasswords.size(); i++) {
            String description = userPasswords.get(i).getDescription();
            String password = encodeService.decryptData(userPasswords.get(i).getPassword());
            stringBuilder.append(String.format("\n" + PASSWORD_LIST_FORMAT, i + 1, description, password));
        }

        userStateCache.setState(userId, State.IN_LIST);

        return new Response(stringBuilder.toString(), keyboardCreator.createInListKeyboard());
    }

    /**
     * Валидирует команду
     *
     * @param splitCommand команда, разделённая по пробелам
     * @return true, если команда валидна
     */
    private boolean isValidCommand(String[] splitCommand) {
        return splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH;
    }
}
