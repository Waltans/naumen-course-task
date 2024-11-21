package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер удаления пароля
 */
@Component
public class DeleteHandler implements CommandHandler {

    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private final RemindScheduler remindScheduler;

    public DeleteHandler(PasswordService passwordService, UserStateCache userStateCache, ValidationService validationService, RemindScheduler remindScheduler) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.remindScheduler = remindScheduler;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, DELETE_STEP_1);

            return new Response(ENTER_PASSWORD_INDEX, DELETE_STEP_1);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (!validationService.isValidPasswordIndex(userId, passwordIndex)){
            userStateCache.setState(userId, NONE);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), NONE);
        }

        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        String uuid = userPasswords.get(passwordIndexInSystem).getUuid();
        String description = userPasswords.get(passwordIndexInSystem).getDescription();
        passwordService.deletePassword(uuid);
        remindScheduler.cancelReminderIfScheduled(uuid);
        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(String.format(PASSWORD_DELETED_MESSAGE, description), NONE);
    }
}
