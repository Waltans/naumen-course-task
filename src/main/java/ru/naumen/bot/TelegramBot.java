package ru.naumen.bot;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.naumen.service.CommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Телеграм бот
 */
@Component
class TelegramBot extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private final CommandService commandService;
    private final String botName;

    public TelegramBot(@Value("${bot.token}") String botToken,
                       CommandService commandService,
                       @Value("${bot.name}") String botName) {
        super(botToken);
        this.commandService = commandService;
        this.botName = botName;
    }

    /**
     * Метод инициализации бота, выполняется после поднятия контекста
     */
    @EventListener({ContextRefreshedEvent.class})
    public void initialize() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            log.error("Error initializing Bot", e);
            System.exit(1);
        }
    }

    /**
     * Обрабатывает полученное сообщение, создаёт нового пользователя,
     * если он впервые взаимодействует с ботом
     *
     * @param update обновление
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            long userId = update.getMessage().getFrom().getId();
            String username = update.getMessage().getFrom().getUserName();

            String response = commandService.performCommand(messageText, userId, username);
            sendMessageToChat(response, chatId);
        }
    }

    /**
     * Отправляет сообщение в чат
     *
     * @param message - сообщение
     * @param id - id чата, куда отправляем сообщение
     */
    private void sendMessageToChat(String message, String id) {
        SendMessage tgMessage = new SendMessage();
        tgMessage.setText(message);
        tgMessage.setChatId(id);
        try {
            execute(tgMessage);
        } catch (TelegramApiException e) {
            log.error("Message could not be sent", e);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}