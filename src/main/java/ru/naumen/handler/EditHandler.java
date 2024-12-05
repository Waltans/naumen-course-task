package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.ComplexityFormatException;
import ru.naumen.exception.PasswordLengthException;
import ru.naumen.exception.PasswordNotFoundException;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.*;
import static ru.naumen.bot.constants.Information.PASSWORD_UPDATED_MESSAGE;
import static ru.naumen.bot.constants.Parameters.*;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_INDEX;

/**
 * Хэндлер изменения пароля
 */
@Component("/edit")
public class EditHandler implements CommandHandler {
    private final Logger log = LoggerFactory.getLogger(EditHandler.class);
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final List<Integer> paramsCount = List.of(3, 4);

    /**
     * Длина команды редактирования, если передано описание
     */
    private static final int EDIT_COMMAND_LENGTH_HAS_DESCRIPTION = 5;

    public EditHandler(PasswordService passwordService, UserStateCache userStateCache) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.EDIT_STEP_1);
            return new Response(ENTER_PASSWORD_INDEX);
        }

        if (!isValid(splitCommand)) {
            return new Response(INCORRECT_COMMAND_RESPONSE);
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);

        if (!passwordService.isValidPasswordIndex(passwordIndex, userId)) {
            userStateCache.setState(userId, State.NONE);
            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex));
        }

        int length = Integer.parseInt(splitCommand[2]);
        String complexity = splitCommand[3];

        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        try {
            UserPassword passwordByUuid = passwordService.findPasswordByUuid(uuid);
            String description = passwordByUuid.getDescription();
            String newPassword = passwordService.generatePassword(length, complexity);

            if (splitCommand.length == EDIT_COMMAND_LENGTH_HAS_DESCRIPTION) {
                description = splitCommand[4];
            }
            passwordService.updatePassword(uuid, description, newPassword);
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword));
        } catch (PasswordLengthException e) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(LENGTH_ERROR_MESSAGE);
        } catch (ComplexityFormatException e) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(COMPLEXITY_ERROR_MESSAGE);
        } catch (PasswordNotFoundException e) {
            log.error(e.getMessage());
            userStateCache.setState(userId, State.NONE);

            return new Response(String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex));
        }
    }

    @Override
    public boolean isValid(String[] command) {
        if (!paramsCount.contains(command.length - 1)) {
            return false;
        }
        if (!(isNumber(command[1]) && isNumber(command[2]))) {
            return false;
        }
        return ((command.length - 1) == 3 || (command.length - 1) == 4)
                && (!isValidLength(Integer.parseInt(command[2])) || !isValidComplexity(command[3]));
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
