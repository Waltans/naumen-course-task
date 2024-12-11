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

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Requests.ENTER_CODE_PHRASE;
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
    private final KeyboardCreator keyboardCreator;

    /**
     * Сообщение, когда невозможно удалить пароли
     */
    private static final String CANT_RUN_OPERATION = "Невозможно запустить операцию";

    /**
     * Сообщение, когда нет кодового слова
     */
    private static final String NO_CODE_WORD = "У вас не установлено кодовое слово";

    /**
     * Сообщение, когда произошла ошибка дешифорвания кодового слова
     */
    private static final String DECRYPT_ERROR = "Ошибка при дешифровании кодового слова";

    /**
     * Количество параметров команды
     */
    private static final int PARAMS_COUNT = 2;

    public ClearPasswordHandler(UserStateCache userStateCache,
                                UserService userService,
                                PasswordService passwordService,
                                EncodeService encodeService,
                                KeyboardCreator keyboardCreator) {
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

            return new Response(ENTER_CODE_PHRASE, keyboardCreator.createEmptyKeyboard());
        }

        if (!isValidCommand(splitCommand)) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
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

                return new Response(NO_CODE_WORD, keyboardCreator.createMainKeyboard());
            }
        } catch (UserNotFoundException e) {
            userStateCache.clearParamsForUser(userId);
            log.error("Пользователь не найден", e);

            return new Response(Errors.USER_NOT_FOUND, keyboardCreator.createMainKeyboard());
        } catch (DecryptException e) {
            userStateCache.clearParamsForUser(userId);
            userStateCache.setState(userId, NONE);
            log.error("Ошибка при дешифровании кодового слова", e);

            return new Response(DECRYPT_ERROR, keyboardCreator.createMainKeyboard());
        }
    }

    /**
     * Определяет форму слова "пароль" в зависимости от количества.
     *
     * @param count - количество паролей
     * @return форма слова "пароль"
     */
    private String getPasswordForm(int count) {
        if (count % 100 / 10 == 1) {
            return "паролей";
        }

        return switch (count % 10) {
            case 1 -> "пароль";
            case 2, 3, 4 -> "пароля";
            default -> "паролей";
        };
    }

    /**
     * Определяет форму слова "Удалить" в зависимости от количества.
     *
     * @param count - количество удаленных паролей
     * @return форма слова "удалить"
     */
    private String getDeleteForm(int count) {
        if (count % 100 / 10 == 1) {
            return "Удалено";
        }

        return count % 10 == 1 ? "Удален" : "Удалено";
    }

    /**
     * Валидирует команду
     *
     * @param splitCommand команда, разделённая по пробелам
     * @return true, если команда валидна
     */
    private boolean isValidCommand(String[] splitCommand) {
        return (splitCommand.length - COMMAND_WITHOUT_PARAMS_LENGTH) == PARAMS_COUNT;
    }
}
