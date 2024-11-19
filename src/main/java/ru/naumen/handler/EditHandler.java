package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.PasswordNotFoundException;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.EDIT_STEP_1;
import static ru.naumen.model.State.NONE;

/**
 * Хэндлер изменения пароля
 */
@Component
public class EditHandler implements CommandHandler {

    private final Logger log = LoggerFactory.getLogger(EditHandler.class);
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;

    public EditHandler(PasswordService passwordService, UserStateCache userStateCache, ValidationService validationService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, EDIT_STEP_1);
            return new Response(ENTER_PASSWORD_INDEX, EDIT_STEP_1);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (!validationService.isValidPasswordIndex(userId, passwordIndex)) {
            userStateCache.setState(userId, NONE);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), NONE);
        }

        int length = Integer.parseInt(splitCommand[2]);
        String complexity = splitCommand[3];

        if (!validationService.isValidLength(length)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(LENGTH_ERROR_MESSAGE, NONE);
        }
        if (!validationService.isValidComplexity(complexity)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(COMPLEXITY_ERROR_MESSAGE, NONE);
        }

        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        UserPassword passwordByUuid;
        try {
            passwordByUuid = passwordService.findPasswordByUuid(uuid);
        } catch (PasswordNotFoundException e) {
            log.error(e.getMessage());
            userStateCache.setState(userId, NONE);

            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), NONE);
        }
        String description = passwordByUuid.getDescription();

        String newPassword = passwordService.generatePassword(length, complexity);
        if (splitCommand.length == EDIT_COMMAND_LENGTH_HAS_DESCRIPTION) {
            description = splitCommand[4];
        }

        passwordService.updatePassword(uuid, description, newPassword);
        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword), NONE);
    }
}
