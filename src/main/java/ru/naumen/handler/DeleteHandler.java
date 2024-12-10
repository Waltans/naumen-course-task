package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.remind.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.*;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_INDEX;
import static ru.naumen.model.State.NONE;

/**
 * Хэндлер удаления пароля
 */
@Component("/del")
public class DeleteHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final KeyboardCreator keyboardCreator;
    private final RemindScheduler remindScheduler;

    /**
     * Сообщение об удалении пароля
     */
    private static final String PASSWORD_DELETED_MESSAGE = "Удалён пароль для сайта %s";

    /**
     * Количество параметров команды
     */
    private static final int PARAMS_COUNT = 1;

    public DeleteHandler(PasswordService passwordService,
                         UserStateCache userStateCache,
                         KeyboardCreator keyboardCreator,
                         RemindScheduler remindScheduler) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.keyboardCreator = keyboardCreator;
        this.remindScheduler = remindScheduler;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.DELETE_STEP_1);

            return new Response(ENTER_PASSWORD_INDEX, keyboardCreator.createEmptyKeyboard());
        }

        if (!isValidCommand(splitCommand)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
        }

        int passwordIndex;
        try {
            passwordIndex = Integer.parseInt(splitCommand[1]);
        } catch (NumberFormatException e) {
            userStateCache.setState(userId, State.IN_LIST);

            return new Response(INDEX_ERROR_MESSAGE, keyboardCreator.createInListKeyboard());
        }

        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (!passwordService.isValidPasswordIndex(passwordIndex, userId)) {
            userStateCache.setState(userId, State.IN_LIST);

            return new Response(
                    String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex),
                    keyboardCreator.createInListKeyboard()
            );
        }

        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        String uuid = userPasswords.get(passwordIndexInSystem).getUuid();
        String description = userPasswords.get(passwordIndexInSystem).getDescription();
        passwordService.deletePassword(uuid);
        remindScheduler.cancelRemindIfScheduled(uuid);
        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(
                String.format(PASSWORD_DELETED_MESSAGE, description),
                keyboardCreator.createMainKeyboard()
        );
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
