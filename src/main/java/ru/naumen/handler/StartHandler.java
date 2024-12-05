package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.UserService;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.WELCOME_MESSAGE;

/**
 * Хэндлер команды запуска
 */
@Component("/start")
public class StartHandler implements CommandHandler {

    private final UserService userService;
    private final UserStateCache userStateCache;

    public StartHandler(UserService userService, UserStateCache userStateCache) {
        this.userService = userService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand == null || splitCommand.length == 0) {
            return new Response(INCORRECT_COMMAND_RESPONSE);
        }

        userService.createUserIfUserNotExists(userId);
        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(WELCOME_MESSAGE);
    }

    @Override
    public boolean isValid(String[] command) {
        return true;
    }
}

