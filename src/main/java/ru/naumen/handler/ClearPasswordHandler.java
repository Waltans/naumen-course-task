package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.constants.Errors;
import ru.naumen.bot.constants.Parameters;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.DecryptException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.model.User;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.UserService;

import static ru.naumen.model.State.NONE;

/**
 * Хэндлер по удалению нескольких паролей при заданном кодовом слове
 */
@Component("/clear")
public class ClearPasswordHandler implements CommandHandler {

    private final Logger log = LoggerFactory.getLogger(ClearPasswordHandler.class);
    private final UserStateCache userStateCache;
    private final UserService userService;
    private final PasswordService passwordService;
    private final EncodeService encodeService;
    private static final String ADD_CODE_PHRASE = "Введите кодовое слово";
    private static final String CANT_RUN_OPERATION = "Невозможно запустить операцию";
    private static final String NO_CODEWORD = "У вас не установлено кодовое слово";
    private static final String DECRYPT_ERROR = "Ошибка при дешифровании кодового слова";
    private final KeyboardCreator keyboardCreator;
    public ClearPasswordHandler(UserStateCache userStateCache, UserService userService, PasswordService passwordService, EncodeService encodeService, KeyboardCreator keyboardCreator) {
        this.userStateCache = userStateCache;
        this.userService = userService;
        this.passwordService = passwordService;
        this.encodeService = encodeService;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == Parameters.COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.CLEAR_1);

            return new Response(ADD_CODE_PHRASE, keyboardCreator.createEmptyKeyboard());
        }

        try {
            if (userService.isExistCodeWord(userId)) {
                User user = userService.getUserById(userId);
                String codePhrase = encodeService.decryptData(user.getCodePhrase());
                if (codePhrase.equals(splitCommand[1])) {
                    int countDeletedPassword;
                    if (splitCommand[2].equalsIgnoreCase(Parameters.CLEAR_ALL_PARAM)) {
                        countDeletedPassword = passwordService.deleteAllUserPassword(userId);
                    } else {
                        countDeletedPassword = passwordService
                                .deletePasswordByStartWord(userId, splitCommand[2]);
                    }
                    userStateCache.clearParamsForUser(userId);
                    userStateCache.setState(userId, NONE);
                    String passwordForm = getPasswordForm(countDeletedPassword);
                    String deleteForm = getDeleteForm(countDeletedPassword);

                    return new Response(
                            deleteForm + " " + countDeletedPassword + " " + passwordForm,
                            keyboardCreator.createMainKeyboard());
                } else {
                    userStateCache.setState(userId, NONE);
                    userStateCache.clearParamsForUser(userId);

                    return new Response(CANT_RUN_OPERATION, keyboardCreator.createMainKeyboard());
                }
            } else {
                userStateCache.setState(userId, NONE);
                userStateCache.clearParamsForUser(userId);

                return new Response(NO_CODEWORD, keyboardCreator.createMainKeyboard());
            }
        } catch (UserNotFoundException e) {
            userStateCache.clearParamsForUser(userId);
            log.error("Пользователь не найден");

            return new Response(Errors.USER_NOT_FOUND, keyboardCreator.createMainKeyboard());
        } catch (DecryptException e) {
            userStateCache.clearParamsForUser(userId);
            userStateCache.setState(userId, NONE);
            log.error("Ошибка при дешифровании кодового слова", e);

            return new Response(DECRYPT_ERROR, keyboardCreator.createMainKeyboard());
        }
    }

    /**
     * Получить форму слова "пароля"
     *
     * @param countDeletedPassword - количество паролей для удаления
     * @return - форму слова "пароль"
     */
    private String getPasswordForm(int countDeletedPassword) {
        int preLastDigit = countDeletedPassword % 100 / 10;
        String passwordForm = "пароль";
        if (preLastDigit == 1) {
            return passwordForm;
        }

        return switch (countDeletedPassword % 10) {
            case 1 -> "пароль";
            case 2, 3, 4 -> "пароля";
            default -> "паролей";
        };
    }

    /**
     * Получить форму слова "Удалить"
     *
     * @param countDeletedPassword - количество паролей для удаления
     * @return - форма слова "удалить"
     */
    private String getDeleteForm(int countDeletedPassword) {
        int preLastDigit = countDeletedPassword % 100 / 10;
        String passwordForm = "Удален";
        if (preLastDigit == 1) {
            return passwordForm;
        }

        return switch (countDeletedPassword % 10) {
            case 1 -> "Удален";
            default -> "Удалено";
        };
    }
}
