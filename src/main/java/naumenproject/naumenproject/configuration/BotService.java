package naumenproject.naumenproject.configuration;

import naumenproject.naumenproject.service.CommandService;
import naumenproject.naumenproject.service.MessageService;
import naumenproject.naumenproject.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class BotService extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(BotService.class);
    private final UserService userService;
    private final CommandService commandService;

    @Value("${bot.name}")
    private String botName;

    public BotService(@Value("${bot.token}") String botToken, UserService userService, CommandService commandService) {
        super(botToken);
        this.userService = userService;
        this.commandService = commandService;
    }

    /**
     * Обрабатывает полученное сообщение, создаёт нового пользователя,
     * если он впервые взаимодействует с ботом
     * @param update обновление
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            long userId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getFrom().getUserName();

            if (!userService.checkUserExistsByTelegramId(userId)) {
                userService.createUser(userId, username);
            }

            String response = commandService.performCommand(messageText, userId);
            sendMessageToChat(response, chatId);
        }
    }

    private void sendMessageToChat(String message, String id) {
        SendMessage tgMessage = new SendMessage();
        tgMessage.setText(message);
        tgMessage.setChatId(id);
        try {
            execute(tgMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}