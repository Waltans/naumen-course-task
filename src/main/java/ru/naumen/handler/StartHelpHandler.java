package ru.naumen.handler;

import org.springframework.stereotype.Component;
import ru.naumen.bot.command.Command;
import ru.naumen.bot.command.CommandFinder;
import ru.naumen.bot.Response;
import ru.naumen.bot.UserStateCache;
import ru.naumen.exception.CommandNotFoundException;
import ru.naumen.service.UserService;

import static ru.naumen.bot.constants.Errors.INCORRECT_COMMAND_RESPONSE;
import static ru.naumen.bot.constants.Information.WELCOME_MESSAGE;
import static ru.naumen.model.State.*;

/**
 * Хэндлер команд запуска и справки
 */
@Component
public class StartHelpHandler implements CommandHandler {

    private final UserService userService;
    private final UserStateCache userStateCache;
    private final CommandFinder commandFinder;

    public StartHelpHandler(UserService userService, UserStateCache userStateCache, CommandFinder commandFinder) {
        this.userService = userService;
        this.userStateCache = userStateCache;
        this.commandFinder = commandFinder;
    }

    @Override
    public Response handle(String[] splitCommand, long userId) {
        if (splitCommand == null || splitCommand.length == 0) {
            return new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        }

        Response response;



        try {
            Command command = commandFinder.findCommand(splitCommand[0]);
            switch (command) {
                case START -> {
                    userService.createUserIfUserNotExists(userId);
                    response = new Response(WELCOME_MESSAGE, NONE);
                }
                case HELP -> response = new Response(WELCOME_MESSAGE, NONE);

                default -> response = new Response(INCORRECT_COMMAND_RESPONSE, NONE);

            }
        } catch (CommandNotFoundException e) {
            response = new Response(INCORRECT_COMMAND_RESPONSE, NONE);
        }

        userStateCache.setState(userId, NONE);
        userStateCache.clearParamsForUser(userId);

        return response;
    }

}
