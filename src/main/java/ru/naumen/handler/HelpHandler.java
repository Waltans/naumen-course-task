package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.WELCOME_MESSAGE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;

/**
 * Хэндлер команды справки
 */
@Component("/help")
public class HelpHandler implements CommandHandler {

    private final UserStateCache userStateCache;
    private final KeyboardCreator keyboardCreator;

    public HelpHandler(UserStateCache userStateCache,
                       KeyboardCreator keyboardCreator) {
        this.userStateCache = userStateCache;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (!isValidCommand(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
        }

        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(WELCOME_MESSAGE, keyboardCreator.createMainKeyboard());
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
