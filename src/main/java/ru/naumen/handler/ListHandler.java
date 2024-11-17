package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер получения списка паролей
 */
@Component
public class ListHandler implements CommandHandler {
    private final EncodeService encodeService;
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;

    public ListHandler(EncodeService encodeService, PasswordService passwordService, UserStateCache userStateCache) {
        this.encodeService = encodeService;
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
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

        userStateCache.setState(userId, IN_LIST);

        return new Response(stringBuilder.toString(), IN_LIST);
    }
}
