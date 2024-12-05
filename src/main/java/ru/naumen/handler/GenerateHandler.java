package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.ComplexityFormatException;
import ru.naumen.exception.PasswordLengthException;
import ru.naumen.model.State;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.*;
import static ru.naumen.bot.constants.Information.PASSWORD_GENERATED_MESSAGE;
import static ru.naumen.bot.constants.Parameters.*;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_LENGTH;

/**
 * Хэндлер генерации
 */
@Component("/generate")
public class GenerateHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final List<Integer> params = List.of(2);

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

        if (!isValid(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE);
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

    @Override
    public boolean isValid(String[] command) {
        if (!params.contains(command.length - 1)) {
            return false;
        }
        if (!isNumber(command[1])) {
            return false;
        }
        return ((command.length - 1) == 2)
                && (!isValidLength(Integer.parseInt(command[1])) || !isValidComplexity(command[2]));
    }

    /**
     * Проверяем корректная ли длина
     *
     * @param length - длина
     * @return - true, если длина корректная
     */
    private boolean isValidLength(Integer length) {
        return length >= MINIMUM_PASSWORD_LENGTH && length <= MAXIMUM_PASSWORD_LENGTH;
    }

    /**
     * Проверяет, является ли строка числом
     *
     * @param string строка
     * @return true, если строка состоит из числа
     */
    private boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Метод проверяет что указана правильная сложность (от 1 до 3)
     *
     * @param complexity - сложность пароля
     * @return корректна ли сложность
     */
    public boolean isValidComplexity(String complexity) {
        return complexity.equals("1")
                || complexity.equals("2")
                || complexity.equals("3")
                || complexity.equals(COMPLEXITY_EASY)
                || complexity.equals(COMPLEXITY_MEDIUM)
                || complexity.equals(COMPLEXITY_HARD);
    }
}
