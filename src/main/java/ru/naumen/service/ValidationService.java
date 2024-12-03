package ru.naumen.service;

import org.springframework.stereotype.Service;
import ru.naumen.bot.command.Command;
import ru.naumen.bot.command.CommandFinder;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.CommandNotFoundException;
import ru.naumen.model.State;

import java.util.List;

import static ru.naumen.bot.constants.Parameters.*;


/**
 * Сервис валидации введённых данных
 */
@Service
public class ValidationService {

    private final PasswordService passwordService;

    private final UserStateCache userStateCache;

    private final CommandFinder commandFinder;

    /**
     * Минимальная длина пароля
     */
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    /**
     * Максимальная длина пароля
     */
    private static final int MAXIMUM_PASSWORD_LENGTH = 128;

    public ValidationService(PasswordService passwordService, UserStateCache userStateCache, CommandFinder commandFinder) {
        this.passwordService = passwordService;
        this.userStateCache = userStateCache;
        this.commandFinder = commandFinder;
    }

    /**
     * Проверяет корректность команды
     *
     * @param splitCommand разделённая по пробелам команда
     * @param userId - ID пользователя
     * @return true, если команда и её параметры корректны, иначе false
     */
    boolean isValidCommand(String[] splitCommand, long userId) {
        String commandString = splitCommand[0];
        int paramsCount = splitCommand.length - 1;

        State state = userStateCache.getUserState(userId);
        if (state != null && !state.equals(State.NONE) && !state.equals(State.IN_LIST)) {
            return switch (state) {
                case SAVE_STEP_1, SAVE_STEP_2, EDIT_STEP_4, FIND_STEP_1, GENERATION_STEP_2, EDIT_STEP_3 -> true;
                case GENERATION_STEP_1, EDIT_STEP_1, EDIT_STEP_2, DELETE_STEP_1 ->
                        isNumber(commandString);
                case SORT_STEP_1 -> isValidSortType(commandString);
                default -> false;
            };
        }

        List<Integer> params;

        try {
            Command command = commandFinder.findCommand(commandString);
            params = command.getValidParamCounts();
        } catch (CommandNotFoundException e) {
            params = List.of();
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
    public boolean isValidComplexity(String complexity) {
        return complexity.equals("1") ||
               complexity.equals("2") ||
               complexity.equals("3") ||
               complexity.equals(COMPLEXITY_EASY) ||
               complexity.equals(COMPLEXITY_MEDIUM) ||
               complexity.equals(COMPLEXITY_HARD);
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
        return sortType.equals(BY_DATE)
                || sortType.equals(BY_DESCRIPTION);
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
