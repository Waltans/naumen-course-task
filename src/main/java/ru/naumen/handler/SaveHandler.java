package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер сохранения пароля
 */
@Component
public class SaveHandler implements CommandHandler {

    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final RemindScheduler remindScheduler;
    private final ValidationService validationService;
    private final Logger log = LoggerFactory.getLogger(SaveHandler.class);

    public SaveHandler(PasswordService passwordService, UserStateCache userStateCache, RemindScheduler remindScheduler, ValidationService validationService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.remindScheduler = remindScheduler;
        this.validationService = validationService;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, SAVE_STEP_1);

            return new Response(ENTER_PASSWORD, SAVE_STEP_1);
        }

        try {
            String password = splitCommand[1];
            if (splitCommand.length == SAVE_COMMAND_LENGTH_NO_DESCRIPTION) {
                passwordService.createUserPassword(password, "Неизвестно", userId);
            } else if (splitCommand.length == SAVE_COMMAND_LENGTH_WITH_REMIND) {
                String description = splitCommand[2];
                int daysToRemind = Integer.parseInt(splitCommand[3]);

                if (!validationService.isValidDays(daysToRemind)) {
                    return new Response(DAYS_ERROR_MESSAGE, NONE);
                }

                long millisToRemind = daysToRemind * MILLIS_IN_A_DAY;
                String passwordUuid = passwordService.createUserPassword(password, description, userId);
                remindScheduler.scheduleRemind(String.format(REMIND_MESSAGE_PASSWORD, description), userId, passwordUuid, millisToRemind);
            } else {
                String description = splitCommand[2];
                passwordService.createUserPassword(password, description, userId);
            }
            userStateCache.clearParamsForUser(userId);

            return new Response(PASSWORD_SAVED_MESSAGE, NONE);
        } catch (UserNotFoundException e){
            log.error("Ошибка при сохранении пароля - не найден пользователь", e);
            userStateCache.clearParamsForUser(userId);

            return new Response(USER_NOT_FOUND, NONE);
        }
        catch (EncryptException e){
            log.error("Ошибка шифрования при сохранении пароля", e);
            userStateCache.clearParamsForUser(userId);

            return new Response(ENCRYPT_ERROR, NONE);
        }
    }
}
