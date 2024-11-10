package ru.naumen.service;

import ru.naumen.model.UserPassword;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static ru.naumen.bot.Constants.*;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {

    private final EncodeService encodeService;
    private final PasswordService passwordService;
    private static final String VALIDATION_OK = "ok";
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

    public CommandService(EncodeService encodeService, PasswordService passwordService) {
        this.encodeService = encodeService;
        this.passwordService = passwordService;
    }

    /**
     * Обрабатывает команду, введённую пользователем
     * @param message текст команды
     * @param userId ID пользователя
     * @return ответ на команду
     */
    public String performCommand(String message, long userId) {
        String[] splitCommand = message.split(" ");
        String response;

        if (!isValidCommand(splitCommand)) {
            return INCORRECT_COMMAND_RESPONSE;
        }

        String command = splitCommand[0];
        switch (command) {
            case "/generate" -> response = generatePassword(splitCommand);
            case "/save" -> response = savePassword(splitCommand, userId);
            case "/list" -> response = getUserPasswords(userId);
            case "/del" -> response = deletePassword(splitCommand, userId);
            case "/edit" -> response = updatePassword(splitCommand, userId);
            case "/help", "/start" -> response = WELCOME_MESSAGE;
            default -> response = INCORRECT_COMMAND_RESPONSE;
        }

        return response;
    }

    /**
     * Проверяет корректность команды и количества её параметров
     * @param splitCommand разделённая по пробелам команда
     * @return true, если команда и её параметры корректны, иначе false
     */
    private boolean isValidCommand(String[] splitCommand) {
        String command = splitCommand[0];
        if (commandsAndNumberOfParams.containsKey(command) &&
                commandsAndNumberOfParams.get(command).contains(splitCommand.length - 1)) {

            return switch (command) {
                case "/generate" -> isNumber(splitCommand[1]) && isNumber(splitCommand[2]);
                case "/del" -> isNumber(splitCommand[1]);
                case "/edit" -> isNumber(splitCommand[1]) && isNumber(splitCommand[2]) && isNumber(splitCommand[3]);
                default -> true;
            };
        }
        return false;
    }

    /**
     * Проверяет параметры генерации пароля
     * @param length длина
     * @param complexity сложность
     * @return ok, если параметры валидны, иначе - сообщение с ошибкой
     */
    private String validateGenerationParameters(int length, int complexity) {
        if (length < 8 || length > 128) {
            return LENGTH_ERROR_MESSAGE;
        }
        if (!(complexity == 1 || complexity == 2 || complexity == 3)) {
            return COMPLEXITY_ERROR_MESSAGE;
        }
        return VALIDATION_OK;
    }

    /**
     * Проверяет, является ли строка числом
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
     * @param splitCommand разделённая по пробелам команда
     * @return сообщение с паролем или с ошибкой
     */
    private String generatePassword(String[] splitCommand) {
        int length = Integer.parseInt(splitCommand[1]);
        int complexity = Integer.parseInt(splitCommand[2]);

        String paramsValidationResult = validateGenerationParameters(length, complexity);
        if (!paramsValidationResult.equals(VALIDATION_OK)) {
            return paramsValidationResult;
        }

        String password = passwordService.generatePasswordWithComplexity(length, complexity);
        return String.format(PASSWORD_GENERATED_MESSAGE, password);
    }

    /**
     * Сохраняет пароль для пользователя. Если описание не передано, туда подставляется "Неизвестно"
     * @param splitCommand разделённая по пробелам команда
     * @param userId ID пользователя
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
     * @param userId ID пользователя
     * @return сообщение об удалении или об ошибке в случае некорректного ID
     */
    private String deletePassword(String[] splitCommand, long userId) {
        int passwordIndex = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        if (passwordIndex > userPasswords.size() || passwordIndex <= 0) {
            return String.format(PASSWORD_NOT_FOUND_MESSAGE, passwordIndex);
        }

        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        String description = userPasswords.get(passwordIndex - 1).getDescription();
        passwordService.deletePassword(uuid);
        return String.format(PASSWORD_DELETED_MESSAGE, description);
    }

    /**
     * Обновляет пароль, генерирует новый по заданным параметрам.
     * Если описание не передано, туда подставляется null (т.е. не обновляется)
     * @param splitCommand разделённая по пробелам команда
     * @param userId ID пользователя
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

        String paramsValidationResult = validateGenerationParameters(length, complexity);
        if (!paramsValidationResult.equals(VALIDATION_OK)) {
            return paramsValidationResult;
        }

        String uuid = userPasswords.get(passwordIndex - 1).getUuid();
        String description = passwordService.findPasswordByUuid(uuid).getDescription();

        String newPassword = passwordService.generatePasswordWithComplexity(length, complexity);
        if (splitCommand.length == EDIT_COMMAND_LENGTH_HAS_DESCRIPTION) {
            description = splitCommand[4];
        }

        passwordService.updatePassword(uuid, description, newPassword);
        return String.format(PASSWORD_UPDATED_MESSAGE, description, newPassword);
    }
}
