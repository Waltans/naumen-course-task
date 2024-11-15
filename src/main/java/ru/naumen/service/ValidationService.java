package ru.naumen.service;

import org.springframework.stereotype.Service;
import ru.naumen.bot.Command;
import ru.naumen.bot.UserStateCache;
import ru.naumen.model.State;

import java.util.List;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;


/**
 * Сервис валидации введённых данных
 */
@Service
public class ValidationService {

    private final PasswordService passwordService;

    private final UserStateCache userStateCache;

    public ValidationService(PasswordService passwordService, UserStateCache userStateCache) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
    }

    /**
     * Проверяет корректность команды
     *
     * @param splitCommand разделённая по пробелам команда
     * @param userId - ID пользователя
     * @return true, если команда и её параметры корректны, иначе false
     */
    boolean isValidCommand(String[] splitCommand, long userId) {
        String command = splitCommand[0];
        int paramsCount = splitCommand.length - 1;

        State state = userStateCache.getUserState(userId);
        if (state != null && !state.equals(NONE) && !state.equals(IN_LIST)) {
            return switch (state) {
                case SAVE_STEP_1, SAVE_STEP_2, EDIT_STEP_4, FIND_STEP_1 -> true;
                case GENERATION_STEP_1, GENERATION_STEP_2, EDIT_STEP_1, EDIT_STEP_2, EDIT_STEP_3, DELETE_STEP_1 ->
                        isNumber(command);
                case SORT_STEP_1 -> isValidSortType(command);
                default -> false;
            };
        }

        List<Integer> params;
        if (Command.commandsAndNumberOfParams.containsKey(command)) {
            params = Command.commandsAndNumberOfParams.get(command);
        } else {
            params = Command.commandsAndNumberOfParams
                    .getOrDefault(Command.commandKeyMapping
                            .getOrDefault(command, ""), List.of());
        }

        return params != null &&
                params.contains(paramsCount);
    }

    /**
     * Проверяет валидность индекса пароля
     * @param userId id пользователя
     * @param passwordIndex индекс
     * @return true, если индекс валиден
     */
    public boolean isValidPasswordIndex(long userId, int passwordIndex) {
        long passwordsSize = passwordService.countPasswordsByUserId(userId);
        return (passwordIndex <= passwordsSize) && (passwordIndex >= 1);
    }

    /**
     * Метод проверяет что указана правильная сложность (от 1 до 3)
     *
     * @param complexity - сложность пароля
     * @return корректна ли сложность
     */
    public boolean isValidComplexity(int complexity) {
        return complexity == 1 || complexity == 2 || complexity == 3;
    }

    /**
     * Проверяем корректность введённой длины пароля
     *
     * @param length - длина пароля
     * @return корректна ли длина
     */
    public boolean isValidLength(int length) {
        return length >= MINIMUM_PASSWORD_LENGTH && length <= MAXIMUM_PASSWORD_LENGTH;
    }

    /**
     * Проверяем валидность параметров команды /edit
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    public boolean areNumbersEditCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]) && isNumber(splitCommand[2]) && isNumber(splitCommand[3]);
    }

    /**
     * Проверяем валидность параметров команды /del
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    public boolean areNumbersDeleteCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]);
    }

    /**
     * Проверяем валидность параметров команды /generate
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    public boolean areNumbersGenerationCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]) && isNumber(splitCommand[2]);
    }

    /**
     * Проверяет, является ли валидным тип сортировки
     * @param sortType тип сортировки
     * @return true, если тип введен корректно
     */
    private boolean isValidSortType(String sortType) {
        return sortType.equals(Command.BY_DATE)
                || sortType.equals(Command.BY_DESCRIPTION);
    }

    /**
     * Проверяет, является ли строка числом
     *
     * @param string строка
     * @return true, если строка состоит из числа
     */
    private boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
