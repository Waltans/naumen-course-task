package ru.naumen.service;

import org.springframework.stereotype.Service;
import ru.naumen.bot.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.handler.*;

import static ru.naumen.bot.Constants.*;
import static ru.naumen.model.State.*;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private final NonCommandHandler nonCommandHandler;
    private final GenerateHandler generateHandler;
    private final EditHandler editHandler;
    private final DeleteHandler deleteHandler;
    private final SaveHandler saveHandler;
    private final ListHandler listHandler;
    private final StartHelpHandler startHelpHandler;
    private final SortHandler sortHandler;
    private final FindHandler findHandler;

    public CommandService(UserStateCache userStateCache, ValidationService validationService, NonCommandHandler nonCommandHandler, GenerateHandler generateHandler, EditHandler editHandler, DeleteHandler deleteHandler, SaveHandler saveHandler, ListHandler listHandler, StartHelpHandler startHelpHandler, SortHandler sortHandler, FindHandler findHandler) {
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.nonCommandHandler = nonCommandHandler;
        this.generateHandler = generateHandler;
        this.editHandler = editHandler;
        this.deleteHandler = deleteHandler;
        this.saveHandler = saveHandler;
        this.listHandler = listHandler;
        this.startHelpHandler = startHelpHandler;
        this.sortHandler = sortHandler;
        this.findHandler = findHandler;
    }

    /**
     * Обрабатывает команду, введённую пользователем
     *
     * @param message  текст команды
     * @param userId   ID пользователя
     * @param username имя пользователя
     * @return ответ на команду и состояние пользователя
     */
    public Response performCommand(String message, long userId, String username) {
        String[] splitCommand = message.split(" ");

        if (!validationService.isValidCommand(splitCommand, userId)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, userStateCache.getTotalUserState().get(userId));
        }

        return doCommand(userId, splitCommand, username);
    }

    /**
     * Метод принимает команду и исполняет её
     * @param userId - ID пользователя
     * @param splitCommand - разделенная команда
     * @return - результат обработки команды
     */
    private Response doCommand(long userId, String[] splitCommand, String username) {
        return switch (splitCommand[0]) {
            case Command.GENERATE, Command.GENERATE_KEYBOARD -> generateHandler.generatePassword(splitCommand, userId);
            case Command.SAVE, Command.SAVE_KEYBOARD -> saveHandler.savePassword(splitCommand, userId);
            case Command.LIST, Command.LIST_KEYBOARD -> listHandler.getUserPasswords(userId);
            case Command.DELETE, Command.DELETE_KEYBOARD -> deleteHandler.deletePassword(splitCommand, userId);
            case Command.EDIT, Command.EDIT_KEYBOARD -> editHandler.updatePassword(splitCommand, userId);
            case Command.HELP, Command.HELP_KEYBOARD -> startHelpHandler.helpCommand(userId);
            case Command.START, Command.MENU_KEYBOARD -> startHelpHandler.startCommand(userId, username);
            case Command.SORT, Command.SORT_KEYBOARD -> sortHandler.sortPasswords(splitCommand, userId);
            case Command.FIND, Command.FIND_KEYBOARD -> findHandler.findPasswords(splitCommand, userId);
            default -> performNotCommandMessage(splitCommand, userId);
        };
    }

    /**
     * Обработка сообщение, которое не является командой
     *
     * @param splitCommand - входящее сообщение разделенное пробелами
     * @param userId       - ID пользователя
     * @return Состояние пользователя и ответ
     */
    private Response performNotCommandMessage(String[] splitCommand, long userId) {
        if (splitCommand.length > 1) {
            return new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        }
        final String command = splitCommand[0];
        return switch (userStateCache.getTotalUserState().get(userId)) {
            case GENERATION_STEP_1 -> nonCommandHandler.getPasswordLength(command, userId, GENERATION_STEP_2);
            case GENERATION_STEP_2 -> nonCommandHandler.getComplexity(command, userId, NONE, null);
            case SAVE_STEP_1 -> nonCommandHandler.getPassword(command, userId, SAVE_STEP_2);
            case SAVE_STEP_2, EDIT_STEP_4 -> nonCommandHandler.getDescription(command, userId, NONE, null);
            case EDIT_STEP_1, DELETE_STEP_1 -> nonCommandHandler.getIndexPassword(command, userId);
            case EDIT_STEP_2 -> nonCommandHandler.getPasswordLength(command, userId, EDIT_STEP_3);
            case EDIT_STEP_3 -> nonCommandHandler.getComplexity(command, userId, EDIT_STEP_4, ENTER_PASSWORD_DESCRIPTION);
            case SORT_STEP_1 -> nonCommandHandler.getSortType(command, userId);
            case FIND_STEP_1 -> nonCommandHandler.getSearchRequest(command, userId);
            default -> new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        };
    }
}
