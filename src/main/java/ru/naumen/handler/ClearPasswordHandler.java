package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.DecryptException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.UserService;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.CLEAR_2;
import static ru.naumen.model.State.NONE;

/**
 * Хэндлер по удалению нескольких паролей при заданном кодовом слове
 */
@Component
public class ClearPasswordHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(ClearPasswordHandler.class);
    private final UserStateCache userStateCache;
    private final UserService userService;
    private final PasswordService passwordService;
    private final EncodeService encodeService;

    public ClearPasswordHandler(UserStateCache userStateCache, UserService userService, PasswordService passwordService, EncodeService encodeService) {
        this.userStateCache = userStateCache;
        this.userService = userService;
        this.passwordService = passwordService;
        this.encodeService = encodeService;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.CLEAR_1);

            return new Response(ADD_CODE_PHRASE, State.CLEAR_1);
        }

        try {
            if (userService.isExistCodeWord(userId)) {
                User user = userService.getUserById(userId);
                String codePhrase = encodeService.decryptData(user.getCodePhrase());
                if (codePhrase.equals(splitCommand[1])) {
                    passwordService.deletePasswordByStartWord(userId, splitCommand[2]);
                    userStateCache.clearParamsForUser(userId);
                    userStateCache.setState(userId, NONE);

                    return new Response(String.format(CLEAR_SUCCESS, splitCommand[2]), NONE);
                } else {
                    userStateCache.setState(userId, NONE);
                    userStateCache.clearParamsForUser(userId);

                    return new Response(CODE_UNCORRECTED, CLEAR_2);
                }
            } else {
                userStateCache.setState(userId, NONE);
                userStateCache.clearParamsForUser(userId);

                return new Response(ENTER_CODE, NONE);
            }
        } catch (UserNotFoundException e) {
            userStateCache.clearParamsForUser(userId);
            log.error("Пользователь не найден");

            return new Response(USER_NOT_FOUND, NONE);
        } catch (DecryptException e) {
            userStateCache.clearParamsForUser(userId);
            userStateCache.setState(userId, NONE);
            log.error("Ошибка при дешифровании кодового слова", e);

            return new Response(DECRYPT_ERROR, NONE);
        }
    }
}
