package ru.naumen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.naumen.exception.DecryptException;
import ru.naumen.exception.EncryptException;
import ru.naumen.exception.PasswordNotFoundException;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.UserPassword;

import java.util.List;
import java.util.Map;

import static ru.naumen.bot.Constants.*;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {

    private final Logger log = LoggerFactory.getLogger(CommandService.class);
    private final EncodeService encodeService;
    private final PasswordService passwordService;
    private final UserService userService;
    private static final int SAVE_COMMAND_LENGTH_NO_DESCRIPTION = 2;
    private static final int EDIT_COMMAND_LENGTH_HAS_DESCRIPTION = 5;

    /**
     * Карта, в которой ключи - команды, значения - список допустимых
     * количеств параметров, передаваемых вместе с командой.
     */
    private final Map<String, List<Integer>> commandsAndNumberOfParams = Map.of (
                "/start", List.of(0),
                "/generate", List.of(2),
                "/save", List.of(1, 2),
                "/list", List.of(0),
                "/edit", List.of(3, 4),
                "/del", List.of(1),
                "/help", List.of(0)
            );

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

        return doCommand(userId, splitCommand);
    }

    /**
     * Метод принимает команду и исполняет её
     * @param userId - ID пользователя
     * @param splitCommand - разделенная команда
     * @return - результат обработки команды
     */
    private String doCommand(long userId, String[] splitCommand) {
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
        return commandsAndNumberOfParams.containsKey(command) &&
                commandsAndNumberOfParams.get(command).contains(paramsCount);
    }

    /**
     * Проверяем валидность параметров команды /edit
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    private boolean areNumbersEditCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]) && isNumber(splitCommand[2]) && isNumber(splitCommand[3]);
    }

    /**
     * Проверяем валидность параметров команды /delete
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    private boolean areNumbersDeleteCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]);
    }

    /**
     * Проверяем валидность параметров команды /generation
     *
     * @param splitCommand - разделенный список параметров
     * @return - true, если все параметры удовлетворяют
     */
    private boolean areNumbersGenerationCommandParams(String[] splitCommand) {
        return isNumber(splitCommand[1]) && isNumber(splitCommand[2]);
    }

    /**
     * Метод проверяет что указана правильная сложность (от 1 до 3)
     *
     * @param complexity - сложность пароля
     * @return корректна ли сложность
     */
    private boolean checkComplexity(int complexity) {
        return complexity == 1 || complexity == 2 || complexity == 3;
    }

    /**
     * Проверяем корректность введённой длины пароля
     *
     * @param length - длина пароля
     * @return корректна ли длина
     */
    private boolean checkLength(int length) {
        return length >= 8 && length <= 128;
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
        if (!areNumbersGenerationCommandParams(splitCommand)) {
            return INCORRECT_COMMAND_RESPONSE;
        }
        int length = Integer.parseInt(splitCommand[1]);
        int complexity = Integer.parseInt(splitCommand[2]);

        if (!checkComplexity(complexity)) {
            return COMPLEXITY_ERROR_MESSAGE;
        }
        if (!checkLength(length)) {
            return LENGTH_ERROR_MESSAGE;
        }
        String password = passwordService.generatePassword(length, complexity);

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
        try {
            String password = splitCommand[1];
            if (splitCommand.length == SAVE_COMMAND_LENGTH_NO_DESCRIPTION) {
                passwordService.createUserPassword(password, "Неизвестно", userId);
            } else {
                String description = splitCommand[2];
                passwordService.createUserPassword(password, description, userId);
            }

            return PASSWORD_SAVED_MESSAGE;
        } catch (UserNotFoundException e){
            log.error("Ошибка при сохранении пароля - не найден пользователь", e);
            return USER_NOT_FOUND;
        }
        catch (EncryptException e){
            log.error("Ошибка шифрования при сохранении пароля", e);
            return ENCRYPT_EXCEPTION;
        }
    }

    /**
     * Получает список паролей пользователя
     *
     * @param userId ID пользователя
     * @return сообщение со списком
     */
    private String getUserPasswords(long userId) {
        try {
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
        } catch (DecryptException e){
            log.error("Произошла ошибка дешифрования", e);
            return DECRYPT_EXCEPTION;
        }
    }

    /**
     * Удаляет пароль
     *
     * @param userId ID пользователя
     * @return сообщение об удалении или об ошибке в случае некорректного ID
     */
    private String deletePassword(String[] splitCommand, long userId) {
        if (!areNumbersDeleteCommandParams(splitCommand)) {
            return INCORRECT_COMMAND_RESPONSE;
        }
        int passwordIndex = Integer.parseInt(splitCommand[1]);

        long passwordsSize = passwordService.countPasswordsByUserId(userId);
        if (passwordIndex > passwordsSize || passwordIndex <= 0) {
            return String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex);
        }
        // Пользователь получает список начиная с 1
        int passwordIndexInSystem = passwordIndex - 1;

        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
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
        if (!areNumbersEditCommandParams(splitCommand)) {
            return INCORRECT_COMMAND_RESPONSE;
        }
        int passwordIndex = Integer.parseInt(splitCommand[1]);

        long passwordsSize = passwordService.countPasswordsByUserId(userId);
        if (passwordIndex > passwordsSize || passwordIndex <= 0) {
            return String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex);
        }

        int length = Integer.parseInt(splitCommand[2]);
        int complexity = Integer.parseInt(splitCommand[3]);

        if (!checkLength(length)){
            return LENGTH_ERROR_MESSAGE;
        }
        if (!checkComplexity(complexity)) {
            return COMPLEXITY_ERROR_MESSAGE;
        }

        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        UserPassword passwordByUuid;
        try {
            passwordByUuid = passwordService.findPasswordByUuid(uuid);
        } catch (PasswordNotFoundException e) {
            return String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex);
        }
        String description = passwordByUuid.getDescription();

        String newPassword = passwordService.generatePassword(length, complexity);
        if (splitCommand.length == EDIT_COMMAND_LENGTH_HAS_DESCRIPTION) {
            description = splitCommand[4];
        }
        passwordService.updatePassword(uuid, description, newPassword);

        return String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword);
    }
}
