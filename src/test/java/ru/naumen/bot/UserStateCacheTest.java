package ru.naumen.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.naumen.model.State;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ����� ��������� ������ UserStateCache
 */
class UserStateCacheTest {

    private UserStateCache userStateCache;

    /**
     * ������ ����� ������ ���� ����� ������ ������
     */
    @BeforeEach
    void setUp() {
        userStateCache = new UserStateCache();
    }

    /**
     * ���� ������ �������� ��������� ������������, ���� ��� ��� � ����
     */
    @Test
    void getUserStateNotInCache() {
        State result = userStateCache.getUserState(12345L);

        assertEquals(State.NONE, result);
    }

    /**
     * ���� ������ �������� ��������� ������������, ���� �� ���� � ����
     * ������ ��������� ����� ��������� ��������� ��� ���������� � ����
     */
    @Test
    void getUserStateUserInCache() {
        userStateCache.setState(12345L, State.FIND_STEP_1);
        State result = userStateCache.getUserState(12345L);

        assertEquals(State.FIND_STEP_1, result);
    }

    /**
     * ���� ������ ��������� ��������� ��� ������� � ����
     */
    @Test
    void setUserStateUserInCache() {
        userStateCache.setState(12345L, State.NONE);
        userStateCache.setState(12345L, State.GENERATION_STEP_1);
        State result = userStateCache.getUserState(12345L);

        assertEquals(State.GENERATION_STEP_1, result);
    }

    /**
     * ���� ������ ��������� ���������� ������������ ��� ���������� � ����
     */
    @Test
    void getUserParamsUserNotInCache() {
        List<String> result = userStateCache.getUserParams(12345L);
        assertTrue(result.isEmpty());
    }

    /**
     * ���� ������ ��������� ���������� ������������ ��� ������� � ����
     * ������ ��������� ����� ���������� ���������� ��� ���������� � ����
     */
    @Test
    void getUserParams_ShouldReturnExistingParams_WhenUserInCache() {
        userStateCache.addParam(12345L, "param1");

        List<String> result = userStateCache.getUserParams(12345L);

        assertEquals(1, result.size());
        assertTrue(result.contains("param1"));
    }

    /**
     * ���� ������ ���������� ���������� ��� ������� � ����
     */
    @Test
    void addParam_ShouldAddMultipleParamsToUser_WhenCalledMultipleTimes() {
        userStateCache.addParam(12345L, "param1");
        userStateCache.addParam(12345L, "param2");

        List<String> result = userStateCache.getUserParams(12345L);

        assertEquals(2, result.size());
        assertTrue(result.contains("param2"));
    }
}
