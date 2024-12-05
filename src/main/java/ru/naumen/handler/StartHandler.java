package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.service.UserService;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.WELCOME_MESSAGE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;

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
        if (!isValidCommand(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE);
        }

        userService.createUserIfUserNotExists(userId);
        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(WELCOME_MESSAGE);
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

