package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.remind.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.constants.Errors;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.ENCRYPT_ERROR;
import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.REMIND_MESSAGE_PASSWORD;
import static ru.naumen.bot.constants.Parameters.*;

/**
 * Хэндлер сохранения пароля
 */
@Component("/save")
public class SaveHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final RemindScheduler remindScheduler;
    private final KeyboardCreator keyboardCreator;
    private final Logger log = LoggerFactory.getLogger(SaveHandler.class);

    /**
     * Сообщение о сохранении пароля
     */
    private static final String PASSWORD_SAVED_MESSAGE = "Пароль успешно сохранён";

    /**
     * Сообщение с запросом на ввод пароля
     */
    private static final String ENTER_PASSWORD_REQUEST = "Введите пароль";

    /**
     * Длина команды сохранения, если не передано описание
     */
    private static final int SAVE_COMMAND_LENGTH_NO_DESCRIPTION = 2;

    /**
     * Длина команды сохранения, если есть напоминание
     */
    private static final int SAVE_COMMAND_LENGTH_WITH_REMIND = 4;

    /**
     * Сообщение, когда пользователь не создан
     */
    private static final String USER_NOT_FOUND = "Пользователь не найден";

    /**
     * Возможные количества параметров команды
     */
    private final List<Integer> params = List.of(1, 2, 3);

    public SaveHandler(PasswordService passwordService,
                       UserStateCache userStateCache,
                       KeyboardCreator keyboardCreator,
                       RemindScheduler remindScheduler) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.remindScheduler = remindScheduler;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.SAVE_STEP_1);

            return new Response(ENTER_PASSWORD_REQUEST, keyboardCreator.createEmptyKeyboard());
        }

        if (!isValidCommand(splitCommand)) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
        }

        try {
            String password = splitCommand[1];
            if (splitCommand.length == SAVE_COMMAND_LENGTH_NO_DESCRIPTION) {
                passwordService.createUserPassword(password, "Неизвестно", userId);
            } else if (splitCommand.length == SAVE_COMMAND_LENGTH_WITH_REMIND) {
                String description = splitCommand[2];
                int daysToRemind = Integer.parseInt(splitCommand[3]);

                if (!isValidDays(daysToRemind)) {
                    return new Response(Errors.DAYS_ERROR_MESSAGE, keyboardCreator.createEmptyKeyboard());
                }

                long millisToRemind = daysToRemind * MILLIS_IN_A_DAY;
                String passwordUuid = passwordService.createUserPassword(password, description, userId);

                Response remindResponse = new Response(String.format(REMIND_MESSAGE_PASSWORD, description),
                        keyboardCreator.createMainKeyboard());
                remindScheduler.scheduleRemind(userId, passwordUuid, millisToRemind, remindResponse);

            } else {
                String description = splitCommand[2];
                passwordService.createUserPassword(password, description, userId);
            }
            userStateCache.clearParamsForUser(userId);
            userStateCache.setState(userId, State.NONE);

            return new Response(PASSWORD_SAVED_MESSAGE, keyboardCreator.createMainKeyboard());
        } catch (UserNotFoundException e) {
            log.error("Ошибка при сохранении пароля - не найден пользователь", e);
            userStateCache.clearParamsForUser(userId);

            return new Response(USER_NOT_FOUND, keyboardCreator.createMainKeyboard());
        } catch (EncryptException e) {
            log.error("Ошибка шифрования при сохранении пароля", e);
            userStateCache.clearParamsForUser(userId);

            return new Response(ENCRYPT_ERROR, keyboardCreator.createMainKeyboard());
        }
    }

    /**
     * Проверяет валидность количества дней до напоминания
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
        return params.contains(splitCommand.length - COMMAND_WITHOUT_PARAMS_LENGTH);
    }
}
