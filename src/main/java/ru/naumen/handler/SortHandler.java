package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;
import ru.naumen.service.EncodeService;
import ru.naumen.service.PasswordService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Хэндлер сортировки паролей
 */
@Component
public class SortHandler {
    private final PasswordService passwordService;
    private final UserStateCache userStateCache;
    private final EncodeService encodeService;

    public SortHandler(PasswordService passwordService, UserStateCache userStateCache, EncodeService encodeService) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.encodeService = encodeService;
    }


    /**
     * Сортирует пароли по указанному в команде параметру
     * @param splitCommand разделённая по пробелам команда
     * @param userId id пользователя
     * @return сообщение со списком паролей
     */
    public Response sortPasswords(String[] splitCommand, Long userId) {
        State currentState = userStateCache.getTotalUserState().get(userId);

        if (currentState.equals(SORT_STEP_1)) {
            String sortType = splitCommand[0];
            List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

            if (!userPasswords.isEmpty()) {
                List<UserPassword> sortedPasswords = new ArrayList<>();
                switch (sortType) {
                    case Command.DATE -> sortedPasswords = userPasswords.stream()
                            .sorted(Comparator.comparing(UserPassword::getLastModifyDate))
                            .toList();
                    case Command.DESCRIPTION -> sortedPasswords = userPasswords.stream()
                                    .sorted((p1, p2) -> p1.getDescription().compareToIgnoreCase(p2.getDescription()))
                                    .toList();
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < sortedPasswords.size(); i++) {
                    String description = sortedPasswords.get(i).getDescription();
                    String password = encodeService.decryptData(sortedPasswords.get(i).getPassword());
                    stringBuilder.append(String.format(PASSWORD_LIST_FORMAT, i + 1, description, password));
                }

                userStateCache.getTotalUserState().put(userId, NONE);
                return new Response(stringBuilder.toString(), NONE);
            } else {
                userStateCache.getTotalUserState().put(userId, NONE);
                return new Response(NO_PASSWORDS_MESSAGE, NONE);
            }
        } else {
            userStateCache.getTotalUserState().put(userId, SORT_STEP_1);
            userStateCache.getTotalUserParams().put(userId, new ArrayList<>());
            return new Response(CHOOSE_SORT_TYPE, SORT_STEP_1);
        }
    }
}
