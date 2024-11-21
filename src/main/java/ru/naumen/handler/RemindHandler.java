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
 * Хэндлер установки напоминания
 */
@Component
public class RemindHandler implements CommandHandler {
    private final RemindScheduler remindScheduler;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private final PasswordService passwordService;

    public RemindHandler(RemindScheduler remindScheduler, UserStateCache userStateCache, ValidationService validationService, PasswordService passwordService) {
        this.remindScheduler = remindScheduler;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.passwordService = passwordService;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, REMIND_STEP_1);
            return new Response(ENTER_PASSWORD_INDEX, REMIND_STEP_1);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        int daysToRemind = Integer.parseInt(splitCommand[2]);
        long millisToRemind = daysToRemind * MILLIS_IN_A_DAY;

        if (!validationService.isValidPasswordIndex(userId, passwordIndex)){
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), NONE);
        }

        if (!validationService.isValidDays(daysToRemind)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(DAYS_ERROR_MESSAGE, NONE);
        }

        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        String description = userPasswords.get(passwordIndexInSystem).getDescription();
        String passwordUuid = userPasswords.get(passwordIndexInSystem).getUuid();

        remindScheduler.scheduleRemind(String.format(REMIND_MESSAGE, description), userId, passwordUuid, millisToRemind);
        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(String.format(REMIND_SET_MESSAGE, description), NONE);
    }
}
