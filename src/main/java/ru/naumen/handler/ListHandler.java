package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.List;

import static ru.naumen.bot.constants.Errors.NO_PASSWORDS_MESSAGE;
import static ru.naumen.bot.constants.Information.PASSWORD_LIST_FORMAT;

/**
 * Хэндлер получения списка паролей
 */
@Component("/list")
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
            return new Response(NO_PASSWORDS_MESSAGE);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userPasswords.size(); i++) {
            String description = userPasswords.get(i).getDescription();
            String password = encodeService.decryptData(userPasswords.get(i).getPassword());
            stringBuilder.append(String.format("\n" + PASSWORD_LIST_FORMAT, i + 1, description, password));
        }

        userStateCache.setState(userId, State.IN_LIST);

        return new Response(stringBuilder.toString());
    }

    @Override
    public boolean isValid(String[] command) {
        return true;
    }
}
