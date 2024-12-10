package ru.naumen.remind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.naumen.bot.Response;

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
     * entityId сущности -> задача с напоминанием
     */
    private final Map<String, ScheduledFuture<?>> scheduledReminders = new ConcurrentHashMap<>();

    public RemindScheduler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Устанавливает напоминание
     *
     * @param response      ответ с напоминанием
     * @param userId        идентификатор пользователя
     * @param entityId      идентификатор сущности для напоминания
     * @param delayInMillis количество миллисекунд до напоминания
     */
    public void scheduleRemind(long userId, String entityId, long delayInMillis, Response response) {
        cancelRemindIfScheduled(entityId);
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            eventPublisher.publishEvent(new ReminderEvent(
                    this,
                    String.valueOf(userId),
                    response));
            scheduledReminders.remove(entityId);
        }, delayInMillis, TimeUnit.MILLISECONDS);

        scheduledReminders.put(entityId, future);
        log.info("Запланировано напоминание, entityId: {}", entityId);
    }

    /**
     * Отменяет напоминание по Id сущности
     *
     * @param entityId идентификатор сущности для напоминания
     */
    public void cancelRemindIfScheduled(String entityId) {
        ScheduledFuture<?> future = scheduledReminders.remove(entityId);
        if (future != null) {
            future.cancel(false);
            log.info("Отменено напоминание, entityId: {}", entityId);
        }
    }
}
