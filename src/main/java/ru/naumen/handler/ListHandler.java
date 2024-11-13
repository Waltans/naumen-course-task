package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.Constants.NO_PASSWORDS_MESSAGE;
import static ru.naumen.bot.Constants.PASSWORD_LIST_FORMAT;
import static ru.naumen.model.State.IN_LIST;
import static ru.naumen.model.State.NONE;

/**
 * Хэндлер получения списка паролей
 */
@Component
public class ListHandler {
    private final EncodeService encodeService;
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;

    public ListHandler(EncodeService encodeService, PasswordService passwordService, UserStateCache userStateCache) {
        this.encodeService = encodeService;
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    /**
     * Получает список паролей пользователя
     *
     * @param userId ID пользователя
     * @return сообщение со списком
     */
    public Response getUserPasswords(long userId) {
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (userPasswords.isEmpty()) {
            return new Response(NO_PASSWORDS_MESSAGE, NONE);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userPasswords.size(); i++) {
            String description = userPasswords.get(i).getDescription();
            String password = encodeService.decryptData(userPasswords.get(i).getPassword());
            stringBuilder.append(String.format(PASSWORD_LIST_FORMAT, i + 1, description, password));
        }

        userStateCache.getTotalUserState().put(userId, IN_LIST);
        return new Response(stringBuilder.toString(), IN_LIST);
    }
}
