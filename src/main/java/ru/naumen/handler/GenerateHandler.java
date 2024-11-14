package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.*;

import java.util.ArrayList;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер генерации
 */
@Component
public class GenerateHandler {

    private final Logger log = LoggerFactory.getLogger(GenerateHandler.class);
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;

    public GenerateHandler(PasswordService passwordService, UserStateCache userStateCache, ValidationService validationService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
    }

    /**
     * Генерирует пароль на основе заданных параметров
     *
     * @param splitCommand разделённая по пробелам команда
     * @return сообщение с паролем или с ошибкой
     */
    public Response generatePassword(String[] splitCommand, Long userId) {
        if (splitCommand.length != COMMAND_WITHOUT_PARAMS_LENGTH &&
                !validationService.areNumbersGenerationCommandParams(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        }
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.getTotalUserState().put(userId, GENERATION_STEP_1);
            userStateCache.getTotalUserParams().put(userId, new ArrayList<>());
            return new Response(ENTER_PASSWORD_LENGTH, GENERATION_STEP_1);
        }

        int length = Integer.parseInt(splitCommand[1]);
        int complexity = Integer.parseInt(splitCommand[2]);

        if (!validationService.isValidComplexity(complexity)) {
            return new Response(COMPLEXITY_ERROR_MESSAGE, NONE);
        }
        if (!validationService.isValidLength(length)) {
            return new Response(LENGTH_ERROR_MESSAGE, NONE);
        }
        String password = passwordService.generatePassword(length, complexity);
        userStateCache.getTotalUserState().put(userId, NONE);

        return new Response(String.format(PASSWORD_GENERATED_MESSAGE, password), NONE);
    }
}
