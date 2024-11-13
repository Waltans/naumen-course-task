package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.PasswordService;

import java.util.ArrayList;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.NONE;
import static ru.naumen.model.State.SAVE_STEP_1;

/**
 * Хэндлер сохранения пароля
 */
@Component
public class SaveHandler {

    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private static final int SAVE_COMMAND_LENGTH_NO_DESCRIPTION = 2;

    public SaveHandler(PasswordService passwordService, UserStateCache userStateCache) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    /**
     * Сохраняет пароль для пользователя. Если описание не передано, туда подставляется "Неизвестно"
     *
     * @param splitCommand разделённая по пробелам команда
     * @param userId       ID пользователя
     * @return сообщение о сохранении
     */
    public Response savePassword(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.getTotalUserState().put(userId, SAVE_STEP_1);
            userStateCache.getTotalUserParams().put(userId, new ArrayList<>());

            return new Response(ENTER_PASSWORD, SAVE_STEP_1);
        }

        String password = splitCommand[1];
        if (splitCommand.length == SAVE_COMMAND_LENGTH_NO_DESCRIPTION) {
            passwordService.createUserPassword(password, "Неизвестно", userId);
        } else {
            String description = splitCommand[2];
            passwordService.createUserPassword(password, description, userId);
        }
        userStateCache.getTotalUserParams().put(userId, new ArrayList<>());
        userStateCache.getTotalUserState().put(userId, NONE);

        return new Response(PASSWORD_SAVED_MESSAGE, NONE);
    }
}
