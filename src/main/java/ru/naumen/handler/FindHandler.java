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
public class FindHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final EncodeService encodeService;

    public FindHandler(PasswordService passwordService, UserStateCache userStateCache, EncodeService encodeService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.encodeService = encodeService;
    }


    /**
     * Ищет пароли
     * @param splitCommand разделённая по пробелам команда
     * @param userId id пользователя
     * @return сообщение со списком паролей
     */
    public Response findPasswords(String[] splitCommand, Long userId) {
        if (splitCommand.length == COMMAND_WITHOUT_PARAMS_LENGTH) {
            userStateCache.getTotalUserState().put(userId, FIND_STEP_1);
            userStateCache.getTotalUserParams().put(userId, new ArrayList<>());
            return new Response(ENTER_SEARCH_REQUEST, FIND_STEP_1);
        }

        String searchRequest = splitCommand[1];
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (!userPasswords.isEmpty()) {
            List<UserPassword> foundPasswords = userPasswords.stream()
                        .filter(pass -> pass.getDescription().startsWith(searchRequest))
                        .toList();

            if (foundPasswords.isEmpty()) {
                userStateCache.getTotalUserState().put(userId, NONE);
                return new Response(NO_PASSWORDS_FOUND, NONE);
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < foundPasswords.size(); i++) {
                String description = foundPasswords.get(i).getDescription();
                String password = encodeService.decryptData(foundPasswords.get(i).getPassword());
                stringBuilder.append(String.format(PASSWORD_LIST_FORMAT, i + 1, description, password));
            }

            userStateCache.getTotalUserState().put(userId, NONE);
            return new Response(stringBuilder.toString(), NONE);
        } else {
            userStateCache.getTotalUserState().put(userId, NONE);
            return new Response(NO_PASSWORDS_MESSAGE, NONE);
        }
    }
}
