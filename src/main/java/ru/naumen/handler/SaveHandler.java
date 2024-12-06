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
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;

/**
 * Хэндлер сохранения пароля
 */
@Component("/save")
public class SaveHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
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
     * Сообщение, когда пользователь не создан
     */
    private static final String USER_NOT_FOUND = "Пользователь не найден";

    /**
     * Сообщение, когда не удалось зашифровать пароль
     */
    private static final String ENCRYPT_ERROR = "Ошибка шифрования пароля";

    /**
     * Возможные количества параметров команды
     */
    private final List<Integer> params = List.of(1, 2);

    public SaveHandler(PasswordService passwordService, UserStateCache userStateCache) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.SAVE_STEP_1);

            return new Response(ENTER_PASSWORD_REQUEST);
        }

        if (!isValidCommand(splitCommand)) {
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
