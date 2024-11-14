package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.UserService;

import static ru.naumen.bot.Constants.WELCOME_MESSAGE;
import static ru.naumen.model.State.NONE;

/**
 * Хэндлер команд запуска и справки
 */
@Component
public class StartHelpHandler {

    private final UserService userService;
    private final UserStateCache userStateCache;

    public StartHelpHandler(UserService userService, UserStateCache userStateCache) {
        this.userService = userService;
        this.userStateCache = userStateCache;
    }

    /**
     * Исполнение /start команды
     *
     * @param userId   - ID пользователя
     * @param username - имя пользователя
     * @return Ответ с приветственным сообщением
     */
    public Response startCommand(long userId, String username) {
        userService.createUserIfUserNotExists(userId, username);

        userStateCache.getTotalUserState().put(userId, NONE);
        return new Response(WELCOME_MESSAGE, NONE);
    }

    /**
     * Исполнение команды /help
     *
     * @param userId ID пользователя
     * @return - приветственное сообщение
     */
    public Response helpCommand(long userId) {
        userStateCache.getTotalUserState().put(userId, NONE);

        return new Response(WELCOME_MESSAGE, NONE);
    }
}
