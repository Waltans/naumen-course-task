package naumenproject.naumenproject.configuration;

import naumenproject.naumenproject.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Компонент по инициализации бота
 */
@Component
public class BotInitialize {

    private final Logger log = LoggerFactory.getLogger(BotInitialize.class);
    private final BotService botService;

    @Autowired
    public BotInitialize(BotService botService) {
        this.botService = botService;
    }

    /**
     * Метод инициализации бота, выполняется после поднятия контекста
     */
    @EventListener({ContextRefreshedEvent.class})
    public void initialize() {
        try{
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(botService);
        } catch (TelegramApiException e) {
            log.error("Error initializing Bot {}", e.getMessage());
            e.printStackTrace();
        }
    }

}