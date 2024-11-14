package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.DELETE_STEP_1;
import static ru.naumen.model.State.NONE;

/**
 * Хэндлер удаления пароля
 */
@Component
public class DeleteHandler {

    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;

    public DeleteHandler(PasswordService passwordService, UserStateCache userStateCache, ValidationService validationService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
    }

    /**
     * Удаляет пароль
     *
     * @param userId ID пользователя
     * @return сообщение об удалении или об ошибке в случае некорректного ID
     */
    public Response deletePassword(String[] splitCommand, long userId) {
        if (splitCommand.length != COMMAND_WITHOUT_PARAMS_LENGTH &&
                !validationService.areNumbersDeleteCommandParams(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        }

        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.getTotalUserState().put(userId, DELETE_STEP_1);
            userStateCache.getTotalUserParams().put(userId, new ArrayList<>());

            return new Response(ENTER_PASSWORD_INDEX, DELETE_STEP_1);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (validationService.isValidPasswordIndex(userId, passwordIndex)){
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), NONE);
        }

        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        String uuid = userPasswords.get(passwordIndexInSystem).getUuid();
        String description = userPasswords.get(passwordIndexInSystem).getDescription();
        passwordService.deletePassword(uuid);
        userStateCache.getTotalUserState().put(userId, NONE);

        return new Response(String.format(PASSWORD_DELETED_MESSAGE, description), NONE);
    }
}
