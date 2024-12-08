package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.constants.Errors;
import ru.naumen.bot.constants.Parameters;
import ru.naumen.bot.constants.Schedules;
import ru.naumen.cache.UserStateCache;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.model.State.NONE;
import static ru.naumen.model.State.REMIND_STEP_1;

/**
 * Хэндлер установки напоминания
 */
@Component("/remind")
public class RemindHandler implements CommandHandler {
    private final RemindScheduler remindScheduler;
    private final UserStateCache userStateCache;
    private final PasswordService passwordService;
    private static final String ENTER_PASSWORD_INDEX = "Введите индекс пароля";
    private final KeyboardCreator keyboardCreator;

    public RemindHandler(RemindScheduler remindScheduler,
                         UserStateCache userStateCache,
                         PasswordService passwordService, KeyboardCreator keyboardCreator) {
        this.remindScheduler = remindScheduler;
        this.userStateCache = userStateCache;
        this.passwordService = passwordService;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == Parameters.COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, REMIND_STEP_1);
            return new Response(ENTER_PASSWORD_INDEX, keyboardCreator.createEmptyKeyboard());
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        int daysToRemind = Integer.parseInt(splitCommand[2]);
        long millisToRemind = daysToRemind * Schedules.MILLIS_IN_A_DAY;

        if (!passwordService.isValidPasswordIndex(passwordIndex, userId)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(
                    String.format(Errors.PASSWORD_NOT_FOUND_MESSAGE, passwordIndex),
                    keyboardCreator.createMainKeyboard());
        }

        if (!isValidDays(daysToRemind)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(Errors.DAYS_ERROR_MESSAGE, keyboardCreator.createMainKeyboard());
        }

        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        String description = userPasswords.get(passwordIndexInSystem).getDescription();
        String passwordUuid = userPasswords.get(passwordIndexInSystem).getUuid();

        remindScheduler.scheduleRemind(
                String.format(Schedules.REMIND_MESSAGE_PASSWORD, description),
                userId, passwordUuid, millisToRemind);
        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(
                String.format(Schedules.REMIND_SET_MESSAGE, description),
                keyboardCreator.createMainKeyboard());
    }

    private boolean isValidDays(int daysToRemind) {
        return daysToRemind >= 3 && daysToRemind <= 90;
    }
}
