package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.service.*;

import static ru.naumen.bot.constants.Errors.COMPLEXITY_ERROR_MESSAGE;
import static ru.naumen.bot.constants.Errors.LENGTH_ERROR_MESSAGE;
import static ru.naumen.bot.constants.Information.PASSWORD_GENERATED_MESSAGE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_LENGTH;
import static ru.naumen.model.State.*;

/**
 * Хэндлер генерации
 */
@Component
public class GenerateHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final ValidationService validationService;

    public GenerateHandler(PasswordService passwordService, UserStateCache userStateCache, ValidationService validationService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.validationService = validationService;
    }
    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, GENERATION_STEP_1);
            return new Response(ENTER_PASSWORD_LENGTH, GENERATION_STEP_1);
        }

        int length = Integer.parseInt(splitCommand[1]);
        String complexity = splitCommand[2];

        if (!validationService.isValidComplexity(complexity)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(COMPLEXITY_ERROR_MESSAGE, NONE);
        }
        if (!validationService.isValidLength(length)) {
            userStateCache.setState(userId, NONE);
            userStateCache.clearParamsForUser(userId);
            return new Response(LENGTH_ERROR_MESSAGE, NONE);
        }
        String password = passwordService.generatePassword(length, complexity);
        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(String.format(PASSWORD_GENERATED_MESSAGE, password), NONE);
    }
}
