package ru.naumen.service;

import org.springframework.stereotype.Service;
import ru.naumen.bot.Command;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.handler.HandlerMapper;
import ru.naumen.handler.NonCommandHandler;

import static ru.naumen.bot.Constants.ENTER_PASSWORD_DESCRIPTION;
import static ru.naumen.bot.Constants.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.model.State.*;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {
    private final UserStateCache userStateCache;
    private final ValidationService validationService;
    private final NonCommandHandler nonCommandHandler;
    private final HandlerMapper handlerMapper;

    public CommandService(UserStateCache userStateCache,
                          ValidationService validationService,
                          NonCommandHandler nonCommandHandler,
                          HandlerMapper handlerMapper) {
        this.userStateCache = userStateCache;
        this.validationService = validationService;
        this.nonCommandHandler = nonCommandHandler;
        this.handlerMapper = handlerMapper;
    }

    /**
     * Обрабатывает команду, введённую пользователем
     *
     * @param message текст команды
     * @param userId  ID пользователя
     * @return ответ на команду и состояние пользователя
     */
    public Response performCommand(String message, long userId) {
        String[] splitCommand = message.split(" ");

        if (!validationService.isValidCommand(splitCommand, userId)) {
            return new Response(INCORRECT_COMMAND_RESPONSE, userStateCache.getUserState(userId));
        }

        return doCommand(userId, splitCommand);
    }

    /**
     * Метод принимает команду и исполняет её
     *
     * @param userId       - ID пользователя
     * @param splitCommand - разделенная команда
     * @return - результат обработки команды
     */
    private Response doCommand(long userId, String[] splitCommand) {
        return switch (splitCommand[0]) {
            case Command.GENERATE, Command.GENERATE_KEYBOARD ->
                    handlerMapper.getHandler(Command.GENERATE).handle(splitCommand, userId);
            case Command.SAVE, Command.SAVE_KEYBOARD ->
                    handlerMapper.getHandler(Command.SAVE).handle(splitCommand, userId);
            case Command.LIST, Command.LIST_KEYBOARD ->
                    handlerMapper.getHandler(Command.LIST).handle(splitCommand, userId);
            case Command.DELETE, Command.DELETE_KEYBOARD ->
                    handlerMapper.getHandler(Command.DELETE).handle(splitCommand, userId);
            case Command.EDIT, Command.EDIT_KEYBOARD ->
                    handlerMapper.getHandler(Command.EDIT).handle(splitCommand, userId);
            case Command.HELP, Command.HELP_KEYBOARD, Command.START, Command.MENU_KEYBOARD ->
                    handlerMapper.getHandler(Command.HELP).handle(splitCommand, userId);
            case Command.SORT, Command.SORT_KEYBOARD ->
                    handlerMapper.getHandler(Command.SORT).handle(splitCommand, userId);
            case Command.FIND, Command.FIND_KEYBOARD ->
                    handlerMapper.getHandler(Command.FIND).handle(splitCommand, userId);
            case Command.REMIND, Command.REMIND_KEYBOARD ->
                    handlerMapper.getHandler(Command.REMIND).handle(splitCommand, userId);
            case Command.ADD_CODE -> handlerMapper.getHandler(Command.ADD_CODE).handle(splitCommand, userId);
            case Command.CLEAR -> handlerMapper.getHandler(Command.CLEAR).handle(splitCommand, userId);

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
        return switch (userStateCache.getUserState(userId)) {
            case GENERATION_STEP_1 -> nonCommandHandler.getPasswordLength(command, userId, GENERATION_STEP_2);
            case GENERATION_STEP_2 -> nonCommandHandler.getComplexity(command, userId, NONE, null);
            case SAVE_STEP_1 -> nonCommandHandler.getPassword(command, userId, SAVE_STEP_2);
            case SAVE_STEP_2 -> nonCommandHandler.getDescription(command, userId, SAVE_STEP_3, null);
            case EDIT_STEP_4 -> nonCommandHandler.getDescription(command, userId, NONE, null);
            case EDIT_STEP_1, DELETE_STEP_1, REMIND_STEP_1 -> nonCommandHandler.getIndexPassword(command, userId);
            case EDIT_STEP_2 -> nonCommandHandler.getPasswordLength(command, userId, EDIT_STEP_3);
            case EDIT_STEP_3 ->
                    nonCommandHandler.getComplexity(command, userId, EDIT_STEP_4, ENTER_PASSWORD_DESCRIPTION);
            case SORT_STEP_1 -> nonCommandHandler.getSortType(command, userId);
            case FIND_STEP_1 -> nonCommandHandler.getSearchRequest(command, userId);
            case REMIND_STEP_2, SAVE_STEP_4 -> nonCommandHandler.getRemindDays(command, userId, NONE);
            case CODE_PHRASE_1, CLEAR_1 -> nonCommandHandler.getCodeWord(command, userId);
            case CLEAR_2 -> nonCommandHandler.getPhraseForClear(command, userId);
            case CLEAR_3, SAVE_STEP_3 -> nonCommandHandler.getAgreement(command, userId);
            default -> new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        };
    }
}
