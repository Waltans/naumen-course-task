package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер поиска паролей
 */
@Component
public class FindHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final EncodeService encodeService;

    public FindHandler(PasswordService passwordService, UserStateCache userStateCache, EncodeService encodeService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.encodeService = encodeService;
    }


    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.setState(userId, FIND_STEP_1);

            return new Response(ENTER_SEARCH_REQUEST, FIND_STEP_1);
        }

        String searchRequest = splitCommand[1];
        List<UserPassword> foundPasswords = passwordService.getUserPasswordsWithPartialDescription(userId, searchRequest);

        if (foundPasswords.isEmpty()) {
            userStateCache.setState(userId, NONE);

            return new Response(NO_PASSWORDS_FOUND, NONE);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < foundPasswords.size(); i++) {
            String description = foundPasswords.get(i).getDescription();
            String password = encodeService.decryptData(foundPasswords.get(i).getPassword());
            stringBuilder.append(String.format(PASSWORD_LIST_FORMAT, i + 1, description, password));
        }

        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return new Response(stringBuilder.toString(), NONE);
    }
}
