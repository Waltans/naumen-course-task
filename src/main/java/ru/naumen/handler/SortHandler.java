package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.cache.UserStateCache;
import ru.naumen.exception.IncorrectSortTypeException;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.SortType;

import java.util.List;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Errors.NO_PASSWORDS_MESSAGE;
import static ru.naumen.bot.constants.Information.PASSWORD_LIST_FORMAT;
import static ru.naumen.bot.constants.Parameters.BY_DATE;
import static ru.naumen.bot.constants.Parameters.BY_DESCRIPTION;
import static ru.naumen.bot.constants.Requests.CHOOSE_SORT_TYPE;

/**
 * Хэндлер сортировки паролей
 */
@Component("/sort")
public class SortHandler implements CommandHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final EncodeService encodeService;

    public SortHandler(PasswordService passwordService, UserStateCache userStateCache, EncodeService encodeService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.encodeService = encodeService;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        State currentState = userStateCache.getUserState(userId);

        if (currentState.equals(State.SORT_STEP_1)) {
            String sortType = splitCommand[0];

            try {
                List<UserPassword> sortedPasswords;
                switch (sortType) {
                    case BY_DATE -> sortedPasswords = passwordService.getUserPasswordsSorted(userId, SortType.BY_DATE);
                    case BY_DESCRIPTION ->
                            sortedPasswords = passwordService.getUserPasswordsSorted(userId, SortType.BY_DESCRIPTION);
                    default -> {
                        return new Response(INCORRECT_COMMAND_RESPONSE);
                    }
                }

                if (sortedPasswords.isEmpty()) {
                    userStateCache.setState(userId, State.NONE);
                    return new Response(NO_PASSWORDS_MESSAGE);
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < sortedPasswords.size(); i++) {
                    String description = sortedPasswords.get(i).getDescription();
                    String password = encodeService.decryptData(sortedPasswords.get(i).getPassword());
                    stringBuilder.append(String.format("\n" + PASSWORD_LIST_FORMAT, i + 1, description, password));
                }

                userStateCache.setState(userId, State.NONE);
                userStateCache.clearParamsForUser(userId);

                return new Response(stringBuilder.toString());
            } catch (IncorrectSortTypeException e) {
                userStateCache.setState(userId, State.IN_LIST);
                return new Response(INCORRECT_COMMAND_RESPONSE);
            }

        } else {
            userStateCache.setState(userId, State.SORT_STEP_1);
            return new Response(CHOOSE_SORT_TYPE);
        }
    }

    @Override
    public boolean isValid(String[] command) {
        return true;
    }
}
