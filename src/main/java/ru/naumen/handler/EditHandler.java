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

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.EDIT_STEP_1;
import static ru.naumen.model.State.NONE;

/**
 * Хэндлер изменения пароля
 */
@Component
public class EditHandler {

    private final Logger log = LoggerFactory.getLogger(EditHandler.class);
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private static final int EDIT_COMMAND_LENGTH_HAS_DESCRIPTION = 5;

    public EditHandler(PasswordService passwordService, UserStateCache userStateCache, ValidationService validationService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
    }

    /**
     * Обновляет пароль, генерирует новый по заданным параметрам.
     * Если описание не передано, туда подставляется null (т.е. не обновляется)
     *
     * @param splitCommand разделённая по пробелам команда
     * @param userId       ID пользователя
     * @return сообщение с паролем или с ошибкой
     */
    public Response updatePassword(String[] splitCommand, long userId) {
        if (splitCommand.length != COMMAND_WITHOUT_PARAMS_LENGTH &&
                !validationService.areNumbersEditCommandParams(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        }
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.getTotalUserState().put(userId, EDIT_STEP_1);
            userStateCache.getTotalUserParams().put(userId, new ArrayList<>());

            return new Response(ENTER_PASSWORD_INDEX, EDIT_STEP_1);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (validationService.isValidPasswordIndex(userId, passwordIndex)){
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), NONE);
        }

        int length = Integer.parseInt(splitCommand[2]);
        int complexity = Integer.parseInt(splitCommand[3]);

        try {
            validationService.validateGenerationParameters(length, complexity);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            userStateCache.getTotalUserState().put(userId, NONE);

            return new Response(e.getMessage(), NONE);
        }

        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        UserPassword passwordByUuid;
        try {
            passwordByUuid = passwordService.findPasswordByUuid(uuid);
        } catch (PasswordNotFoundException e) {
            log.error(e.getMessage());
            userStateCache.getTotalUserState().put(userId, NONE);

            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex), NONE);
        }
        String description = passwordByUuid.getDescription();

        String newPassword = passwordService.generatePassword(length, complexity);
        if (splitCommand.length == EDIT_COMMAND_LENGTH_HAS_DESCRIPTION) {
            description = splitCommand[4];
        }

        passwordService.updatePassword(uuid, description, newPassword);
        userStateCache.getTotalUserState().put(userId, NONE);

        return new Response(String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword), NONE);
    }
}
