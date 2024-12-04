package ru.naumen.repository;

import org.springframework.stereotype.Component;
import ru.naumen.model.State;

import java.util.ArrayList;
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

    /**
     * Возвращает состояние пользователя. Добавляет его в кэш, если его нет
     *
     * @param userId Id пользователя
     */
    public State getUserState(long userId) {
        if (totalUserState.containsKey(userId)) {
            return totalUserState.get(userId);
        } else {
            totalUserState.put(userId, State.NONE);
            return State.NONE;
        }
    }

    /**
     * Возвращает параметры пользователя. Добавляет его в кэш, если его нет
     *
     * @param userId Id пользователя
     */
    public List<String> getUserParams(long userId) {
        if (totalUserParams.containsKey(userId)) {
            return totalUserParams.get(userId);
        } else {
            totalUserParams.put(userId, new ArrayList<>());
            return List.of();
        }
    }

    /**
     * Добавляет параметр пользователю
     *
     * @param userId Id пользователя
     * @param param  параметр
     */
    public void addParam(long userId, String param) {
        if (!totalUserParams.containsKey(userId)) {
            totalUserParams.put(userId, new ArrayList<>());
        }
        totalUserParams.get(userId).add(param);
    }

    /**
     * Устанавливает состояние пользователю
     *
     * @param userId Id пользователя
     * @param state  состояние
     */
    public void setState(long userId, State state) {
        totalUserState.put(userId, state);
    }

    /**
     * Метод очищает параметры для пользователя
     *
     * @param userId - id пользователя
     */
    public void clearParamsForUser(long userId) {
        totalUserParams.remove(userId);
    }
}

