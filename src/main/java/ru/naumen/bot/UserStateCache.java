package ru.naumen.bot;

import org.springframework.stereotype.Component;
import ru.naumen.model.State;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кеш данных пользователей
 */
@Component
public class UserStateCache {

    /**
     * Используется как кеш, где хранятся состояния пользователя
     * ключи - пользователь,
     * значение - состояния
     */
    private final Map<Long, State> totalUserState = new ConcurrentHashMap<>();

    /**
     * Используется как кеш, где хранятся введенные параметры пользователя
     * ключи - пользователь,
     * значение - список параметров
     */
    private final Map<Long, List<String>> totalUserParams = new ConcurrentHashMap<>();

    public Map<Long, State> getTotalUserState() {
        return totalUserState;
    }

    public Map<Long, List<String>> getTotalUserParams() {
        return totalUserParams;
    }
}

