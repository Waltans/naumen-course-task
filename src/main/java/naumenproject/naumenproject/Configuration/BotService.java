package naumenproject.naumenproject.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class BotService extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(BotService.class);

    @Value("${bot.name}")
    private String botName;

    public BotService(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            SendMessage message = SendMessage
                    .builder()
                    .chatId(chatId)
                    .text(messageText)
                    .build();
            log.info(message.getText());
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
