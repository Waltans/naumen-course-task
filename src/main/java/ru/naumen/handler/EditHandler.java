package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.PasswordNotFoundException;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;
import ru.naumen.service.ValidationService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.*;
import static ru.naumen.bot.constants.Information.PASSWORD_UPDATED_MESSAGE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_INDEX;

/**
 * Хэндлер изменения пароля
 */
@Component("/edit")
public class EditHandler implements CommandHandler {

    private final Logger log = LoggerFactory.getLogger(EditHandler.class);
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;

    /**
     * Длина команды редактирования, если передано описание
     */
    private static final int EDIT_COMMAND_LENGTH_HAS_DESCRIPTION = 5;

    public EditHandler(PasswordService passwordService, UserStateCache userStateCache, ValidationService validationService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.EDIT_STEP_1);
            return new Response(ENTER_PASSWORD_INDEX, State.EDIT_STEP_1);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (!validationService.isValidPasswordIndex(userId, passwordIndex)) {
            userStateCache.setState(userId, State.NONE);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), State.NONE);
        }

        int length = Integer.parseInt(splitCommand[2]);
        String complexity = splitCommand[3];

        if (!validationService.isValidLength(length)) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(LENGTH_ERROR_MESSAGE, State.NONE);
        }
        if (!validationService.isValidComplexity(complexity)) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(COMPLEXITY_ERROR_MESSAGE, State.NONE);
        }

        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        UserPassword passwordByUuid;
        try {
            passwordByUuid = passwordService.findPasswordByUuid(uuid);
        } catch (PasswordNotFoundException e) {
            log.error(e.getMessage());
            userStateCache.setState(userId, State.NONE);

            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), State.NONE);
        }
        String description = passwordByUuid.getDescription();

        String newPassword = passwordService.generatePassword(length, complexity);
        if (splitCommand.length == EDIT_COMMAND_LENGTH_HAS_DESCRIPTION) {
            description = splitCommand[4];
        }

        passwordService.updatePassword(uuid, description, newPassword);
        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword), State.NONE);
    }
}
