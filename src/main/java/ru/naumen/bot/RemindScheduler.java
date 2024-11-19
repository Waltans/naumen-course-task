package ru.naumen.bot;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Планировщик напоминаний
 */
@Component
public class RemindScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ApplicationEventPublisher eventPublisher;

    public RemindScheduler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    /**
     * Устанавливает напоминание для пользователя
     *
     * @param message       ссообщение с напоминанием
     * @param userId        идентификатор пользователя
     * @param delayInMillis количество миллисекунд до напоминания
     */
    public void scheduleRemind(String message, long userId, long delayInMillis) {
        scheduler.schedule(() -> {
            eventPublisher.publishEvent(new ReminderEvent(this, String.valueOf(userId), message));
        }, delayInMillis, TimeUnit.MILLISECONDS);
    }
}