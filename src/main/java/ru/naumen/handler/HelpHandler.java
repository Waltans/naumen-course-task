package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.WELCOME_MESSAGE;
import static ru.naumen.model.State.*;

/**
 * Хэндлер команды справки
 */
@Component("/help")
public class HelpHandler implements CommandHandler {

    private final UserStateCache userStateCache;

    public HelpHandler(UserStateCache userStateCache) {
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand == null || splitCommand.length == 0) {
            return new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        }

        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(WELCOME_MESSAGE, NONE);
    }
}
