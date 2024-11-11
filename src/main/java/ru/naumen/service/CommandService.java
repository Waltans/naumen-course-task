package ru.naumen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.naumen.bot.Command;
import ru.naumen.bot.Constants;
import ru.naumen.model.UserPassword;

import java.util.List;
import java.util.Map;

import static ru.naumen.bot.Constants.*;

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

    public CommandService(EncodeService encodeService, PasswordService passwordService, UserService userService) {
        this.encodeService = encodeService;
        this.passwordService = passwordService;
        this.userService = userService;
    }

    /**
     * Обрабатывает команду, введённую пользователем
     *
     * @param message текст команды
     * @param userId  ID пользователя
     * @param username имя пользователя
     * @return ответ на команду
     */
    public String performCommand(String message, long userId, String username) {
        userService.createUserIfUserNotExists(userId, username);

        String[] splitCommand = message.split(" ");
        if (!isValidCommand(splitCommand)) {
            return INCORRECT_COMMAND_RESPONSE;
        }

        return switch (splitCommand[0]) {
            case "/generate" -> generatePassword(splitCommand);
            case "/save" -> savePassword(splitCommand, userId);
            case "/list" -> getUserPasswords(userId);
            case "/del" -> deletePassword(splitCommand, userId);
            case "/edit" -> updatePassword(splitCommand, userId);
            case "/help", "/start" -> WELCOME_MESSAGE;
            default -> INCORRECT_COMMAND_RESPONSE;
        };
    }

    /**
     * Проверяет корректность команды
     *
     * @param splitCommand разделённая по пробелам команда
     * @return true, если команда и её параметры корректны, иначе false
     */
    private boolean isValidCommand(String[] splitCommand) {
        String command = splitCommand[0];
        int paramsCount = splitCommand.length - 1;
        if (Command.commandsAndNumberOfParams .containsKey(command) &&
                Command.commandsAndNumberOfParams.get(command).contains(paramsCount)) {
            return switch (command) {
                case "/generate" -> checkGenerationCommandParams(splitCommand);
                case "/del" -> checkDeleteCommandParams(splitCommand);
                case "/edit" -> checkEditCommandParams(splitCommand);
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
    private String generatePassword(String[] splitCommand) {
        int length = Integer.parseInt(splitCommand[1]);
        int complexity = Integer.parseInt(splitCommand[2]);

        try {
            validateGenerationParameters(length, complexity);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
        String password = passwordService.generatePasswordWithComplexity(length, complexity);

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
        String password = splitCommand[1];
        if (splitCommand.length == SAVE_COMMAND_LENGTH_NO_DESCRIPTION) {
            passwordService.createUserPassword(password, "Неизвестно", userId);
        } else {
            String description = splitCommand[2];
            passwordService.createUserPassword(password, description, userId);
        }

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
        return String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword);
    }
}
