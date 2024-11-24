package ru.naumen.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Command;
import ru.naumen.bot.RemindScheduler;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.UserCodePhraseException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.State;
import ru.naumen.service.UserService;

import static ru.naumen.bot.Constants.*;

/**
 * Хэндлер добавления кодового слова
 */
@Component
public class AddCodePhraseHandler implements CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(AddCodePhraseHandler.class);
    private final UserStateCache userStateCache;
    private final UserService userService;
    private final RemindScheduler scheduler;

    public AddCodePhraseHandler(UserStateCache userStateCache, UserService userService, RemindScheduler scheduler) {
        this.userStateCache = userStateCache;
        this.userService = userService;
        this.scheduler = scheduler;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, State.CODE_PHRASE_1);

            return new Response(Command.ADD_CODE_PHRASE, State.CODE_PHRASE_1);
        }

        try {
            userService.addCodeWordForUser(userId, splitCommand[1]);
        } catch (UserNotFoundException e) {
            userStateCache.clearParamsForUser(userId);
            log.error("User not found", e);

            return new Response(USER_NOT_FOUND, State.NONE);
        } catch (UserCodePhraseException e) {
            userStateCache.clearParamsForUser(userId);
            log.error("User code phrase exist", e);

            return new Response(USER_HAS_CODE_WORD, State.NONE);
        } catch (EncryptException e) {
            userStateCache.clearParamsForUser(userId);
            userStateCache.setState(userId, State.NONE);
            log.error("Encrypt exception", e);

            return new Response(ENCRYPT_ERROR, State.NONE);
        }
        scheduler.scheduleRemind(REMIND_USER_MESSAGE,
                userId, String.format("code-%s", userId), MILLIS_IN_A_DAY * 30);
        userStateCache.setState(userId, State.NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(CODE_ADDED_SUCCESS, State.NONE);
    }
}
