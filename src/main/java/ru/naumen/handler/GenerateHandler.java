package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.exception.ComplexityFormatException;
import ru.naumen.exception.PasswordLengthException;
import ru.naumen.model.State;
import ru.naumen.repository.UserStateCache;
import ru.naumen.service.PasswordService;

import static ru.naumen.bot.constants.Errors.COMPLEXITY_ERROR_MESSAGE;
import static ru.naumen.bot.constants.Errors.LENGTH_ERROR_MESSAGE;
import static ru.naumen.bot.constants.Information.PASSWORD_GENERATED_MESSAGE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_LENGTH;

/**
 * Хэндлер генерации
 */
@Component("/generate")
public class GenerateHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;

    public GenerateHandler(PasswordService passwordService, UserStateCache userStateCache) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.GENERATION_STEP_1);
            return new Response(ENTER_PASSWORD_LENGTH);
        }

        try {
            int length = Integer.parseInt(splitCommand[1]);
            String complexity = splitCommand[2];

            String password = passwordService.generatePassword(length, complexity);
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(String.format(PASSWORD_GENERATED_MESSAGE, password));
        } catch (PasswordLengthException e) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(LENGTH_ERROR_MESSAGE);
        } catch (ComplexityFormatException e) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(COMPLEXITY_ERROR_MESSAGE);
        }
    }
}
