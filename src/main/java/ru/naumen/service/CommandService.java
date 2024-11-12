package ru.naumen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.naumen.bot.Command;
import ru.naumen.model.State;
import ru.naumen.model.UserPassword;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {

    private static final Logger log = LoggerFactory.getLogger(CommandService.class);
    private final EncodeService encodeService;
    private final PasswordService passwordService;
    private final UserService userService;
    private static final int SAVE_COMMAND_LENGTH_NO_DESCRIPTION = 2;
    private static final int EDIT_COMMAND_LENGTH_HAS_DESCRIPTION = 5;
    /**
     * Отображение текущего состояния пользователя,
     * ключи - пользователь,
     * значение - отображение, где ключи состояние, а значения - полученные от пользователя данные
     */
    private final Map<Long, State> totalUserState = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> totalUserParams = new ConcurrentHashMap<>();

    public CommandService(EncodeService encodeService, PasswordService passwordService, UserService userService) {
        this.encodeService = encodeService;
        this.passwordService = passwordService;
        this.userService = userService;
    }

    /**
     * Обрабатывает команду, введённую пользователем
     *
     * @param message  текст команды
     * @param userId   ID пользователя
     * @param username имя пользователя
     * @return ответ на команду
     */
    public String performCommand(String message, long userId, String username) {
        userService.createUserIfUserNotExists(userId, username);

        String[] splitCommand = message.split(" ");
        if (!isValidCommand(splitCommand, userId)) {
            return INCORRECT_COMMAND_RESPONSE;
        }

        return switch (splitCommand[0]) {
            case Command.GENERATE, Command.GENERATE_KEYBOARD -> generatePassword(splitCommand, userId);
            case Command.SAVE, Command.SAVE_KEYBOARD -> savePassword(splitCommand, userId);
            case Command.LIST, Command.LIST_KEYBOARD -> getUserPasswords(userId);
            case Command.DELETE, Command.DELETE_KEYBOARD -> deletePassword(splitCommand, userId);
            case Command.EDIT, Command.EDIT_KEYBOARD -> updatePassword(splitCommand, userId);
            case Command.HELP, Command.START, Command.HELP_KEYBOARD -> WELCOME_MESSAGE;
            default -> performNotCommandMessage(splitCommand, userId);
        };
    }

    private String performNotCommandMessage(String[] splitCommand, long userId) {
        if (splitCommand.length > 1) {
            return INCORRECT_COMMAND_RESPONSE;
        }
        final String command = splitCommand[0];
        return switch (totalUserState.get(userId)) {
            case GENERATION_STEP_1 -> getPasswordLength(command, userId, GENERATION_STEP_2);
            case GENERATION_STEP_2 -> getComplexity(command, userId, NONE, null);
            case SAVE_STEP_1 -> getPassword(command, userId);
            case SAVE_STEP_2 -> getDescription(command, userId, NONE, null);
            case EDIT_STEP_1 -> getIndexPassword(command, userId);
            case EDIT_STEP_2 -> getPasswordLength(command, userId, EDIT_STEP_3);
            case EDIT_STEP_3 -> getComplexity(command, userId, EDIT_STEP_4, ENTER_PASSWORD_DESCRIPTION);
            case EDIT_STEP_4 -> getDescription(command, userId, NONE, null);
            case DELETE_STEP_1 -> getIndexPassword(command, userId);
            default -> INCORRECT_COMMAND_RESPONSE;
        };
    }

    private String getIndexPassword(String index, long userId) {
        totalUserParams.get(userId).add(index);
        State currentState = totalUserState.get(userId);

        if (currentState.equals(EDIT_STEP_1)) {
            totalUserState.put(userId, EDIT_STEP_2);
        } else if (currentState.equals(DELETE_STEP_1)) {
            String[] splitCommand = new String[]{Command.DELETE, index};
            return deletePassword(splitCommand, userId);
        }

        return ENTER_PASSWORD_LENGTH;
    }

    private String getDescription(String description, long userId, State nextState, String response) {
        List<String> params = totalUserParams.get(userId);
        params.add(description);
        State currentState = totalUserState.get(userId);
        totalUserState.put(userId, nextState);

        if (currentState.equals(SAVE_STEP_2)) {
            String[] splitCommand = {Command.SAVE, params.getFirst(), description};

            return savePassword(splitCommand, userId);
        } else if (currentState.equals(EDIT_STEP_4)) {
            String[] splitCommand = {Command.EDIT, params.getFirst(), params.get(1), params.get(2), description};

            return updatePassword(splitCommand, userId);
        }

        return response;
    }

    private String getPassword(String s, long userId) {
        totalUserParams.get(userId).add(s);
        totalUserState.put(userId, SAVE_STEP_2);

        return ENTER_PASSWORD_DESCRIPTION;
    }

    private String getComplexity(String complexity, long userId, State nextState, String response) {
        try {
            checkComplexity(Integer.parseInt(complexity));
            List<String> params = totalUserParams.get(userId);
            params.add(complexity);
            totalUserState.put(userId, nextState);
            if (nextState == null) {
                String[] splitCommand = {Command.EDIT, params.getFirst(), complexity};

                return generatePassword(splitCommand, userId);
            }

            return response;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private String getPasswordLength(String length, long userId, State state) {
        try {
            checkLength(Integer.parseInt(length));
            totalUserState.put(userId, state);
            totalUserParams.get(userId).add(length);
            return ENTER_PASSWORD_COMPLEXITY;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    /**
     * Проверяет корректность команды
     *
     * @param splitCommand разделённая по пробелам команда
     * @param userId
     * @return true, если команда и её параметры корректны, иначе false
     */
    private boolean isValidCommand(String[] splitCommand, long userId) {
        String command = splitCommand[0];
        int paramsCount = splitCommand.length - 1;

        State state = totalUserState.get(userId);
        if (state != null && !state.equals(NONE)) {
            return switch (state) {
                case SAVE_STEP_1, SAVE_STEP_2, EDIT_STEP_4 -> true;
                case GENERATION_STEP_1, GENERATION_STEP_2, EDIT_STEP_1, EDIT_STEP_2, EDIT_STEP_3, DELETE_STEP_1 ->
                        isNumber(command);
                default -> false;
            };
        }

        List<Integer> params = Command.commandsAndNumberOfParams.get(command) == null
                ? Command.commandsAndNumberOfParams.getOrDefault(Command.commandKeyMapping.get(command), List.of())
                : Command.commandsAndNumberOfParams.get(command);

        if (params != null &&
                params.contains(paramsCount)) {
            return switch (command) {
                case Command.GENERATE -> splitCommand.length == 1 || checkGenerationCommandParams(splitCommand);
                case Command.DELETE -> splitCommand.length == 1 || checkDeleteCommandParams(splitCommand);
                case Command.EDIT -> splitCommand.length == 1 || checkEditCommandParams(splitCommand);
                default -> true;
            };
        }
        return false;
    }

    /**
     * Проверяем валидность параметров команды /edit
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    private boolean checkEditCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]) && isNumber(splitCommand[2]) && isNumber(splitCommand[3]);
    }

    /**
     * Проверяем валидность параметров команды /delete
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    private boolean checkDeleteCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]);
    }

    /**
     * Проверяем валидность параметров команды /generation
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    private boolean checkGenerationCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]) && isNumber(splitCommand[2]);
    }

    /**
     * Проверяет параметры генерации пароля
     *
     * @param length     длина
     * @param complexity сложность
     */
    private void validateGenerationParameters(int length, int complexity) {
        checkLength(length);
        checkComplexity(complexity);
    }

    /**
     * Метод проверяет что указана правильная сложность (от 1 до 3)
     *
     * @param complexity - сложность пароля
     */
    private void checkComplexity(int complexity) {
        if (!(complexity == 1 || complexity == 2 || complexity == 3)) {
            throw new IllegalArgumentException(COMPLEXITY_ERROR_MESSAGE);
        }
    }

    /**
     * Проверяем корректность введённой длины пароля
     *
     * @param length - длина пароля
     */
    private void checkLength(int length) {
        if (length < 8 || length > 128) {
            throw new IllegalArgumentException(LENGTH_ERROR_MESSAGE);
        }
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

    /**
     * Генерирует пароль на основе заданных параметров
     *
     * @param splitCommand разделённая по пробелам команда
     * @return сообщение с паролем или с ошибкой
     */
    private String generatePassword(String[] splitCommand, Long userId) {

        if (splitCommand.length == 1) {
            totalUserState.put(userId, GENERATION_STEP_1);
            totalUserParams.put(userId, new ArrayList<>());
            return ENTER_PASSWORD_LENGTH;
        }

        int length = Integer.parseInt(splitCommand[1]);
        int complexity = Integer.parseInt(splitCommand[2]);

        try {
            validateGenerationParameters(length, complexity);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
        String password = passwordService.generatePasswordWithComplexity(length, complexity);
        totalUserState.put(userId, NONE);

        return String.format(PASSWORD_GENERATED_MESSAGE, password);
    }

    /**
     * Сохраняет пароль для пользователя. Если описание не передано, туда подставляется "Неизвестно"
     *
     * @param splitCommand разделённая по пробелам команда
     * @param userId       ID пользователя
     * @return сообщение о сохранении
     */
    private String savePassword(String[] splitCommand, long userId) {
        if (splitCommand.length == 1) {
            totalUserState.put(userId, SAVE_STEP_1);
            totalUserParams.put(userId, new ArrayList<>());

            return ENTER_PASSWORD;
        }

        String password = splitCommand[1];
        if (splitCommand.length == SAVE_COMMAND_LENGTH_NO_DESCRIPTION) {
            passwordService.createUserPassword(password, "Неизвестно", userId);
        } else {
            String description = splitCommand[2];
            passwordService.createUserPassword(password, description, userId);
        }
        totalUserParams.put(userId, new ArrayList<>());

        return PASSWORD_SAVED_MESSAGE;
    }

    /**
     * Получает список паролей пользователя
     *
     * @param userId ID пользователя
     * @return сообщение со списком
     */
    private String getUserPasswords(long userId) {
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (userPasswords.isEmpty()) {
            return NO_PASSWORDS_MESSAGE;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userPasswords.size(); i++) {
            String description = userPasswords.get(i).getDescription();
            String password = encodeService.decryptData(userPasswords.get(i).getPassword());
            stringBuilder.append(String.format(PASSWORD_LIST_FORMAT, i + 1, description, password));
        }

        return stringBuilder.toString();
    }

    /**
     * Удаляет пароль
     *
     * @param userId ID пользователя
     * @return сообщение об удалении или об ошибке в случае некорректного ID
     */
    private String deletePassword(String[] splitCommand, long userId) {
        if (splitCommand.length == 1) {
            totalUserState.put(userId, DELETE_STEP_1);
            totalUserParams.put(userId, new ArrayList<>());

            return ENTER_PASSWORD_INDEX;
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        if (passwordIndex > userPasswords.size() || passwordIndex <= 0) {
            return String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex);
        }
        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        String uuid = userPasswords.get(passwordIndexInSystem).getUuid();
        String description = userPasswords.get(passwordIndexInSystem).getDescription();
        passwordService.deletePassword(uuid);
        totalUserState.put(userId, NONE);

        return String.format(PASSWORD_DELETED_MESSAGE, description);
    }

    /**
     * Обновляет пароль, генерирует новый по заданным параметрам.
     * Если описание не передано, туда подставляется null (т.е. не обновляется)
     *
     * @param splitCommand разделённая по пробелам команда
     * @param userId       ID пользователя
     * @return сообщение с паролем или с ошибкой
     */
    private String updatePassword(String[] splitCommand, long userId) {
        if (splitCommand.length == 1) {
            totalUserState.put(userId, EDIT_STEP_1);
            totalUserParams.put(userId, new ArrayList<>());

            return ENTER_PASSWORD_INDEX;
        }

        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);

        if (passwordIndex > userPasswords.size() || passwordIndex <= 0) {
            return String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex);
        }

        int length = Integer.parseInt(splitCommand[2]);
        int complexity = Integer.parseInt(splitCommand[3]);

        try {
            validateGenerationParameters(length, complexity);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return e.getMessage();
        }

        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        UserPassword passwordByUuid;
        try {
            passwordByUuid = passwordService.findPasswordByUuid(uuid);
        } catch (IllegalArgumentException e) {
            return String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex);
        }
        String description = passwordByUuid.getDescription();

        String newPassword = passwordService.generatePasswordWithComplexity(length, complexity);
        if (splitCommand.length == EDIT_COMMAND_LENGTH_HAS_DESCRIPTION) {
            description = splitCommand[4];
        }

        passwordService.updatePassword(uuid, description, newPassword);
        totalUserState.put(userId, NONE);

        return String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword);
    }
}
