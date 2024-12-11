package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.remind.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.constants.Errors;
import ru.naumen.bot.constants.Parameters;
import ru.naumen.cache.UserStateCache;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.REMIND_MESSAGE_PASSWORD;
import static ru.naumen.bot.constants.Parameters.*;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_INDEX;
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
    private final KeyboardCreator keyboardCreator;

    /**
     * Сообщение о том, что установлено напоминание
     */
    private static final String REMIND_SET_MESSAGE = "Напоминание для пароля %s установлено";

    /**
     * Количество параметров команды
     */
    private static final int PARAMS_COUNT = 2;

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

        if (!isValidCommand(splitCommand)) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        int daysToRemind = Integer.parseInt(splitCommand[2]);
        long millisToRemind = daysToRemind * MILLIS_IN_A_DAY;

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

        Response remindResponse = new Response(String.format(REMIND_MESSAGE_PASSWORD, description),
                keyboardCreator.createMainKeyboard());
        remindScheduler.scheduleRemind(userId, passwordUuid, millisToRemind, remindResponse);

        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(
                String.format(REMIND_SET_MESSAGE, description),
                keyboardCreator.createMainKeyboard());
    }

    /**
     * Проверяет валидность количества дней до напоминания
     *
     * @param daysToRemind дни до напоминания
     * @return true, если количество дней валидно
     */
    private boolean isValidDays(int daysToRemind) {
        return daysToRemind >= MINIMUM_DAYS_TO_REMIND
                && daysToRemind <= MAXIMUM_DAYS_TO_REMIND;
    }

    /**
     * Валидирует команду
     *
     * @param splitCommand команда, разделённая по пробелам
     * @return true, если команда валидна
     */
    private boolean isValidCommand(String[] splitCommand) {
        return (splitCommand.length - COMMAND_WITHOUT_PARAMS_LENGTH) == PARAMS_COUNT;
    }
}
