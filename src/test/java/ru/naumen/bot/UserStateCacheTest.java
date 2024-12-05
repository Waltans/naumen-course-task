package ru.naumen.bot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.naumen.cache.UserStateCache;
import ru.naumen.model.State;

import java.util.List;

/**
 * Класс модульных тестов UserStateCache
 */
class UserStateCacheTest {

    private UserStateCache userStateCache;

    /**
     * Создаёт новый объект кэша перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        userStateCache = new UserStateCache();
    }

    /**
     * Тест метода возврата состояние пользователя, если его нет в кэше
     */
    @Test
    void getUserStateNotInCache() {
        State result = userStateCache.getUserState(12345L);

        Assertions.assertEquals(State.NONE, result);
    }

    /**
     * Тест метода возврата состояние пользователя, если он есть в кэше
     * Заодно тестирует метод установки состояния при отсутствии в кэше
     */
    @Test
    void getUserStateUserInCache() {
        userStateCache.setState(12345L, State.FIND_STEP_1);
        State result = userStateCache.getUserState(12345L);

        Assertions.assertEquals(State.FIND_STEP_1, result);
    }

    /**
     * Тест метода установки состояния при наличии в кэше
     */
    @Test
    void setUserStateUserInCache() {
        userStateCache.setState(12345L, State.NONE);
        userStateCache.setState(12345L, State.GENERATION_STEP_1);
        State result = userStateCache.getUserState(12345L);

        Assertions.assertEquals(State.GENERATION_STEP_1, result);
    }

    /**
     * Тест метода получения параметров пользователя при отсутствии в кэше
     */
    @Test
    void getUserParamsUserNotInCache() {
        List<String> result = userStateCache.getUserParams(12345L);
        Assertions.assertTrue(result.isEmpty());
    }

    /**
     * Тест метода получения параметров пользователя при наличии в кэше
     * Заодно тестирует метод добавления параметров при отсутствии в кэше
     */
    @Test
    void getUserParamsUserInCache() {
        userStateCache.addParam(12345L, "param1");

        List<String> result = userStateCache.getUserParams(12345L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains("param1"));
    }

    /**
     * Тест метода добавления параметров при наличии в кэше
     */
    @Test
    void addParamUserInCache() {
        userStateCache.addParam(12345L, "param1");
        userStateCache.addParam(12345L, "param2");

        List<String> result = userStateCache.getUserParams(12345L);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains("param2"));
    }

    /**
     * Тест метода очистки параметров пользователя
     */
    @Test
    void clearParamsForUser() {
        userStateCache.addParam(12345L, "param1");
        userStateCache.addParam(12345L, "param2");

        userStateCache.clearParamsForUser(12345L);
        Assertions.assertTrue(userStateCache.getUserParams(12345L).isEmpty());
    }
}
