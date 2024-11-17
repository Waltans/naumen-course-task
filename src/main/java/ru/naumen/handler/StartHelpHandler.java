package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.UserService;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.bot.Command.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер команд запуска и справки
 */
@Component
public class StartHelpHandler implements CommandHandler {

    private final UserService userService;
    private final UserStateCache userStateCache;

    public StartHelpHandler(UserService userService, UserStateCache userStateCache) {
        this.userService = userService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        Response response;

        switch (splitCommand[0]) {
            case START, MENU_KEYBOARD -> {
                userService.createUserIfUserNotExists(userId);
                response = new Response(WELCOME_MESSAGE, NONE);
            }
            case HELP, HELP_KEYBOARD -> {
                response = new Response(WELCOME_MESSAGE, NONE);
            }
            default -> {
                response = new Response(INCORRECT_COMMAND_RESPONSE, NONE);
            }
        }

        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return response;
    }
}
