package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.*;
import static ru.naumen.bot.constants.Information.PASSWORD_SAVED_MESSAGE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD;

/**
 * Хэндлер сохранения пароля
 */
@Component("/save")
public class SaveHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final Logger log = LoggerFactory.getLogger(SaveHandler.class);
    private final List<Integer> params = List.of(1, 2);

    /**
     * Длина команды сохранения, если не передано описание
     */
    private static final int SAVE_COMMAND_LENGTH_NO_DESCRIPTION = 2;

    public SaveHandler(PasswordService passwordService, UserStateCache userStateCache) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.SAVE_STEP_1);

            return new Response(ENTER_PASSWORD);
        }

        if (!isValid(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE);
        }

        try {
            String password = splitCommand[1];
            if (splitCommand.length == SAVE_COMMAND_LENGTH_NO_DESCRIPTION) {
                passwordService.createUserPassword(password, "Неизвестно", userId);
            } else {
                String description = splitCommand[2];
                passwordService.createUserPassword(password, description, userId);
            }
            userStateCache.clearParamsForUser(userId);

            return new Response(PASSWORD_SAVED_MESSAGE);
        } catch (UserNotFoundException e) {
            log.error("Ошибка при сохранении пароля - не найден пользователь", e);
            userStateCache.clearParamsForUser(userId);

            return new Response(USER_NOT_FOUND);
        } catch (EncryptException e) {
            log.error("Ошибка шифрования при сохранении пароля", e);
            userStateCache.clearParamsForUser(userId);

            return new Response(ENCRYPT_ERROR);
        }
    }

    @Override
    public boolean isValid(String[] command) {
        return params.contains(command.length - 1);
    }
}
