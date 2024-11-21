package ru.naumen.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Планировщик напоминаний
 */
@Component
public class RemindScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(7);
    private final ApplicationEventPublisher eventPublisher;

    private final Logger log = LoggerFactory.getLogger(RemindScheduler.class);

    /**
     * Карта для хранения запланированных напоминаний
     * UUID сущности -> задача с напоминанием
     */
    private final Map<String, ScheduledFuture<?>> scheduledReminders = new ConcurrentHashMap<>();

    public RemindScheduler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Устанавливает напоминание
     *
     * @param message       сообщение с напоминанием
     * @param userId        идентификатор пользователя
     * @param uuid          идентификатор сущности для напоминания
     * @param delayInMillis количество миллисекунд до напоминания
     */
    public void scheduleRemind(String message, long userId, String uuid, long delayInMillis) {
        cancelRemindIfScheduled(uuid);
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            eventPublisher.publishEvent(new ReminderEvent(this, String.valueOf(userId), message));
            scheduledReminders.remove(uuid);
        }, delayInMillis, TimeUnit.MILLISECONDS);

        scheduledReminders.put(uuid, future);
        log.info("Запланировано напоминание, uuid: {}", uuid);
    }

    /**
     * Отменяет напоминание по uuid
     *
     * @param uuid идентификатор сущности для напоминания
     */
    public void cancelRemindIfScheduled(String uuid) {
        ScheduledFuture<?> future = scheduledReminders.remove(uuid);
        if (future != null) {
            future.cancel(false);
            log.info("Отменено напоминание, uuid: {}", uuid);
        }
    }
}
