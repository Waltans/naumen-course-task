package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.IncorrectSortTypeException;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;
import ru.naumen.service.SortType;

import java.util.ArrayList;
import java.util.List;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Errors.NO_PASSWORDS_MESSAGE;
import static ru.naumen.bot.constants.Information.PASSWORD_LIST_FORMAT;
import static ru.naumen.bot.constants.Parameters.BY_DATE;
import static ru.naumen.bot.constants.Parameters.BY_DESCRIPTION;
import static ru.naumen.bot.constants.Requests.CHOOSE_SORT_TYPE;
import static ru.naumen.model.State.*;

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

        if (currentState.equals(SORT_STEP_1)) {
            String sortType = splitCommand[0];

            try {
                List<UserPassword> sortedPasswords = new ArrayList<>();
                switch (sortType) {
                    case BY_DATE ->
                            sortedPasswords = passwordService.getUserPasswordsSorted(userId, SortType.BY_DATE);
                    case BY_DESCRIPTION ->
                            sortedPasswords = passwordService.getUserPasswordsSorted(userId, SortType.BY_DESCRIPTION);
                }

                if (sortedPasswords.isEmpty()) {
                    userStateCache.setState(userId, NONE);
                    return new Response(NO_PASSWORDS_MESSAGE, NONE);
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < sortedPasswords.size(); i++) {
                    String description = sortedPasswords.get(i).getDescription();
                    String password = encodeService.decryptData(sortedPasswords.get(i).getPassword());
                    stringBuilder.append(String.format("\n" + PASSWORD_LIST_FORMAT, i + 1, description, password));
                }

                userStateCache.setState(userId, NONE);
                userStateCache.clearParamsForUser(userId);

                return new Response(stringBuilder.toString(), NONE);
            } catch (IncorrectSortTypeException e) {
                userStateCache.setState(userId, IN_LIST);
                return new Response(INCORRECT_COMMAND_RESPONSE, IN_LIST);
            }

        } else {
            userStateCache.setState(userId, SORT_STEP_1);
            return new Response(CHOOSE_SORT_TYPE, SORT_STEP_1);
        }
    }
}
