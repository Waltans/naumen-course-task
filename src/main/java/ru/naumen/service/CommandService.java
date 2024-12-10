package ru.naumen.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.naumen.bot.Response;
import ru.naumen.bot.command.Command;
import ru.naumen.cache.UserStateCache;
import ru.naumen.handler.CommandHandler;
import ru.naumen.handler.NonCommandHandler;
import ru.naumen.keyboard.KeyboardCreator;
import ru.naumen.model.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Requests.ENTER_PASSWORD_DESCRIPTION;

/**
 * Класс для работы с командами бота
 */
@Service
public class CommandService {
    private final UserStateCache userStateCache;
    private final NonCommandHandler nonCommandHandler;
    private final KeyboardCreator keyboardCreator;

    /**
     * Хэндлеры команд
     * Название бина (сама команда формата "/command") -> хэндлер
     */
    private final Map<String, CommandHandler> commandHandlers;

    /**
     * Ввод, соответствующий команде -> сама команда
     */
    private final Map<String, Command> commandMap = new HashMap<>();

    public CommandService(UserStateCache userStateCache,
                          NonCommandHandler nonCommandHandler, KeyboardCreator keyboardCreator,
                          Map<String, CommandHandler> commandHandlers) {
        this.userStateCache = userStateCache;
        this.nonCommandHandler = nonCommandHandler;
        this.keyboardCreator = keyboardCreator;
        this.commandHandlers = commandHandlers;

        for (Command cmd : Command.values()) {
            commandMap.put(cmd.getCommand(), cmd);
            commandMap.put(cmd.getKeyboardLabel(), cmd);
        }
    }

    /**
     * Поиск команды по текстовому вводу
     *
     * @param input строка ввода (например, "/edit" или "Изменить")
     * @return команда
     */
    public Optional<Command> findCommand(String input) {
        return Optional.ofNullable(commandMap.get(input));
    }

    /**
     * Обрабатывает команду, введённую пользователем
     *
     * @param message текст команды
     * @param userId  ID пользователя
     * @return ответ на команду
     */
    @Transactional
    public Response performCommand(String message, long userId) {
        String[] splitCommand = message.split(" ");

        return findCommand(splitCommand[0])
                .map(command -> {
                    CommandHandler handler = commandHandlers.get(command.getCommand());
                    return handler.handle(splitCommand, userId);
                })
                .orElseGet(() -> performNotCommandMessage(splitCommand, userId));
    }

    /**
     * Обработка сообщения, которое не является командой
     *
     * @param splitCommand - входящее сообщение разделенное пробелами
     * @param userId       - ID пользователя
     */
    private Response performNotCommandMessage(String[] splitCommand, long userId) {
        if (splitCommand.length > 1) {
            return new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
        }
        final String command = splitCommand[0];

        return switch (userStateCache.getUserState(userId)) {
            case GENERATION_STEP_1 -> nonCommandHandler.getPasswordLength(command, userId, State.GENERATION_STEP_2);
            case GENERATION_STEP_2 -> nonCommandHandler.getComplexity(command, userId, State.NONE, null);
            case SAVE_STEP_1 -> nonCommandHandler.getPassword(command, userId, State.SAVE_STEP_2);
            case SAVE_STEP_2 -> nonCommandHandler.getDescription(command, userId, State.SAVE_STEP_3, null);
            case EDIT_STEP_4 -> nonCommandHandler.getDescription(command, userId, State.NONE, null);
            case EDIT_STEP_1, DELETE_STEP_1, REMIND_STEP_1 -> nonCommandHandler.getIndexPassword(command, userId);
            case EDIT_STEP_2 -> nonCommandHandler.getPasswordLength(command, userId, State.EDIT_STEP_3);
            case EDIT_STEP_3 ->
                    nonCommandHandler.getComplexity(command, userId, State.EDIT_STEP_4, ENTER_PASSWORD_DESCRIPTION);
            case SORT_STEP_1 -> nonCommandHandler.getSortType(command, userId);
            case FIND_STEP_1 -> nonCommandHandler.getSearchRequest(command, userId);
            case REMIND_STEP_2, SAVE_STEP_4 -> nonCommandHandler.getRemindDays(command, userId, State.NONE);
            case CODE_PHRASE_1, CLEAR_1 -> nonCommandHandler.getCodeWord(command, userId);
            case CLEAR_2 -> nonCommandHandler.getPhraseForClear(command, userId);
            case CLEAR_3, SAVE_STEP_3 -> nonCommandHandler.getAgreement(command, userId);
            default -> new Response(INCORRECT_COMMAND_RESPONSE, keyboardCreator.createMainKeyboard());
        };
    }
}
