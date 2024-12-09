package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.constants.Errors;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.UserCodePhraseException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;
import ru.naumen.service.UserService;

import static ru.naumen.bot.constants.Errors.USER_NOT_FOUND;
import static ru.naumen.bot.constants.Parameters.COMMAND_WITHOUT_PARAMS_LENGTH;
import static ru.naumen.bot.constants.Schedules.MILLIS_IN_A_DAY;


/**
 * Хэндлер добавления кодового слова
 */
@Component("/code")
public class AddCodePhraseHandler implements CommandHandler {

    private final Logger log = LoggerFactory.getLogger(AddCodePhraseHandler.class);
    private final UserStateCache userStateCache;
    private final UserService userService;
    private final RemindScheduler scheduler;
    private static final String ADD_CODE_PHRASE = "Введите кодовое слово";
    private static final String CODE_ADDED_SUCCESS = "Кодовое слово успешно установлено";
    private static final String NO_CODEWORD = "Для пользователя уже задано кодовое слово";
    private static final String REMIND_USER_MESSAGE = "Вам необходимо заменить кодовое слово";
    private final KeyboardCreator keyboardCreator;
    public AddCodePhraseHandler(UserStateCache userStateCache, UserService userService, RemindScheduler scheduler, KeyboardCreator keyboardCreator) {
        this.userStateCache = userStateCache;
        this.userService = userService;
        this.scheduler = scheduler;
        this.keyboardCreator = keyboardCreator;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.CODE_PHRASE_1);

            return new Response(ADD_CODE_PHRASE, keyboardCreator.createEmptyKeyboard());
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

            return new Response(Errors.ENCRYPT_ERROR, keyboardCreator.createMainKeyboard());
        }
        scheduler.scheduleRemind(REMIND_USER_MESSAGE,
                userId, String.format("code-%s", userId), MILLIS_IN_A_DAY * 30);
        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(CODE_ADDED_SUCCESS, keyboardCreator.createMainKeyboard());
    }
}
