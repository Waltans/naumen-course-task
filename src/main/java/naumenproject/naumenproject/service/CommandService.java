package naumenproject.naumenproject.service;

import naumenproject.naumenproject.model.UserPassword;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {

    private final MessageService messageService;
    private final PasswordService passwordService;
    private static final String VALIDATION_OK = "ok";

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

    public CommandService(MessageService messageService, PasswordService passwordService) {
        this.messageService = messageService;
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
        String response = "Введена некорректная команда! Справка: /help";

        if (!validateCommand(splitCommand)) {
            return response;
        }

        String command = splitCommand[0];
        switch (command) {
            case "/generate" -> response = generatePassword(splitCommand);
            case "/save" -> response = savePassword(splitCommand, userId);
            case "/list" -> response = getUserPasswords(userId);
            case "/del" -> response = deletePassword(splitCommand, userId);
            case "/edit" -> response = updatePassword(splitCommand, userId);
            case "/help", "/start" -> response = messageService.createWelcomeMessage();
        }

        return response;
    }

    /**
     * Проверяет корректность команды и количества её параметров
     * @param splitCommand разделённая по пробелам команда
     * @return true, если команда и её параметры корректны, иначе false
     */
    private boolean validateCommand(String[] splitCommand) {
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
            return messageService.createMessageLengthError();
        }
        if (!(complexity == 1 || complexity == 2 || complexity == 3)) {
            return messageService.createMessageComplexityError();
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
        return messageService.createMessageWithPassword(password);
    }

    /**
     * Сохраняет пароль для пользователя. Если описание не передано, туда подставляется "Неизвестно"
     * @param splitCommand разделённая по пробелам команда
     * @param userId ID пользователя
     * @return сообщение о сохранении
     */
    private String savePassword(String[] splitCommand, long userId) {
        String password = splitCommand[1];
        if (splitCommand.length == 2) {
            passwordService.createUserPassword(password, "Неизвестно", userId);
        } else {
            String description = splitCommand[2];
            passwordService.createUserPassword(password, description, userId);
        }

        return messageService.createMessagePasswordSaved();
    }

    /**
     * Получает список паролей пользователя
     * @param userId ID пользователя
     * @return сообщение со списком
     */
    private String getUserPasswords(long userId) {
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        return messageService.createMessagePasswordList(userPasswords);
    }

    /**
     * Удаляет пароль
     * @param userId ID пользователя
     * @return сообщение об удалении или об ошибке в случае некорректного ID
     */
    private String deletePassword(String[] splitCommand, long userId) {
        int passwordId = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        if (passwordId > userPasswords.size() || passwordId <= 0) {
            return messageService.createMessageNotFoundError(passwordId);
        }

        String uuid = userPasswords.get(passwordId - 1).getUuid();
        String description = userPasswords.get(passwordId - 1).getDescription();
        passwordService.deletePassword(uuid);
        return messageService.createMessagePasswordDeleted(description);
    }

    /**
     * Обновляет пароль, генерирует новый по заданным параметрам.
     * Если описание не передано, туда подставляется null (т.е. не обновляется)
     * @param splitCommand разделённая по пробелам команда
     * @param userId ID пользователя
     * @return сообщение с паролем или с ошибкой
     */
    private String updatePassword(String[] splitCommand, long userId) {
        int passwordId = Integer.parseInt(splitCommand[1]);
        List<UserPassword> userPasswords = passwordService.getUserPasswords(userId);
        if (passwordId > userPasswords.size() || passwordId <= 0) {
            return messageService.createMessageNotFoundError(passwordId);
        }

        int length = Integer.parseInt(splitCommand[2]);
        int complexity = Integer.parseInt(splitCommand[3]);

        String paramsValidationResult = validateGenerationParameters(length, complexity);
        if (!paramsValidationResult.equals(VALIDATION_OK)) {
            return paramsValidationResult;
        }

        String uuid = userPasswords.get(passwordId - 1).getUuid();
        String description = passwordService.findPasswordByUuid(uuid).getDescription();

        String newPassword = passwordService.generatePasswordWithComplexity(length, complexity);
        if (splitCommand.length == 5) {
            description = splitCommand[4];
        }

        passwordService.updatePassword(uuid, description, newPassword);
        return messageService.createMessagePasswordUpdated(description, newPassword);
    }
}
