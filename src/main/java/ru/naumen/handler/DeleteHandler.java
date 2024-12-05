package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Errors.PASSWORD_NOT_FOUND_MESSAGE;
import static ru.naumen.bot.constants.Information.PASSWORD_DELETED_MESSAGE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_INDEX;

/**
 * Хэндлер удаления пароля
 */
@Component("/del")
public class DeleteHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final List<Integer> params = List.of(1);

    public DeleteHandler(PasswordService passwordService, UserStateCache userStateCache) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (!isValid(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE);
        }

        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.DELETE_STEP_1);

            return new Response(ENTER_PASSWORD_INDEX);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (!passwordService.isValidPasswordIndex(passwordIndex, userId)) {
            userStateCache.setState(userId, State.NONE);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex));
        }

        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        String uuid = userPasswords.get(passwordIndexInSystem).getUuid();
        String description = userPasswords.get(passwordIndexInSystem).getDescription();
        passwordService.deletePassword(uuid);
        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(String.format(PASSWORD_DELETED_MESSAGE, description));
    }

    @Override
    public boolean isValid(String[] command) {
        if (!params.contains(command.length - 1)) {
            return false;
        }
        return (command.length) != 2 || isNumber(command[1]);
    }

    /**
     * Проверяет, является ли строка числом
     *
     * @param string строка
     * @return true, если строка состоит из числа
     */
    private boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
