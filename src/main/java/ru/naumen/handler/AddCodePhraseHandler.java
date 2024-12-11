package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.remind.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.UserCodePhraseException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.service.UserService;

import static ru.naumen.bot.constants.Errors.*;
import static ru.naumen.bot.constants.Parameters.*;
import static ru.naumen.bot.constants.Requests.ENTER_CODE_PHRASE;


/**
 * Хэндлер добавления кодового слова
 */
@Component("/code")
public class AddCodePhraseHandler implements CommandHandler {

    private final Logger log = LoggerFactory.getLogger(AddCodePhraseHandler.class);
    private final UserStateCache userStateCache;
    private final UserService userService;
    private final RemindScheduler remindScheduler;
    private final KeyboardCreator keyboardCreator;

    /**
     * Сообщение об установке кодового слова
     */
    private static final String CODE_ADDED_SUCCESS = "Кодовое слово успешно установлено";

    /**
     * Сообщение о том, что уже задано кодовое слово
     */
    private static final String NO_CODEWORD = "Для пользователя уже задано кодовое слово";

    /**
     * Сообщение о том, что нужно заменить кодовое слово
     */
    private static final String CHANGE_CODE_MESSAGE = "Вам необходимо заменить кодовое слово";

    /**
     * Формат Id для напоминания о смене кодового слова
     */
    private static final String CODE_REMINDER_ID_FORMAT = "code-%s";

    /**
     * Количество дней до напоминания о смене кодового слова
     */
    private static final int DAYS_TO_REMIND_CODE = 30;

    /**
     * Количество параметров команды
     */
    private static final int PARAMS_COUNT = 1;

    public AddCodePhraseHandler(UserStateCache userStateCache,
                                UserService userService,
                                RemindScheduler remindScheduler,
                                KeyboardCreator keyboardCreator) {
        this.userStateCache = userStateCache;
        this.userService = userService;
        this.remindScheduler = remindScheduler;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.CODE_PHRASE_1);

            return new Response(ENTER_CODE_PHRASE, keyboardCreator.createEmptyKeyboard());
        }

        if (!isValidCommand(splitCommand)) {
            userStateCache.setState(userId, State.NONE);
            userStateCache.clearParamsForUser(userId);

            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
        }

        try {
            userService.addCodeWordForUser(userId, splitCommand[1]);
        } catch (UserNotFoundException e) {
            userStateCache.clearParamsForUser(userId);
            log.error("User not found", e);

            return new Response(USER_NOT_FOUND, keyboardCreator.createMainKeyboard());
        } catch (UserCodePhraseException e) {
            userStateCache.clearParamsForUser(userId);
            log.error("User code phrase exist", e);

            return new Response(NO_CODEWORD, keyboardCreator.createMainKeyboard());
        } catch (EncryptException e) {
            userStateCache.clearParamsForUser(userId);
            userStateCache.setState(userId, State.NONE);
            log.error("Encrypt exception", e);

            return new Response(ENCRYPT_ERROR, keyboardCreator.createMainKeyboard());
        }

        long millisToRemind = DAYS_TO_REMIND_CODE * MILLIS_IN_A_DAY;
        String codeReminderId = String.format(CODE_REMINDER_ID_FORMAT, userId);

        Response remindResponse = new Response(CHANGE_CODE_MESSAGE, keyboardCreator.createMainKeyboard());
        remindScheduler.scheduleRemind(userId, codeReminderId, millisToRemind, remindResponse);

        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(CODE_ADDED_SUCCESS, keyboardCreator.createMainKeyboard());
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
