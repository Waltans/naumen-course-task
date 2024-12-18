package ru.naumen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.User;
import ru.naumen.repository.UserRepository;

/**
 * Класс модульных тестов для UserService
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /**
     * Инициализирует моки перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Тест создания объекта пользователя
     */
    @Test
    void testCreateUserIfUserNotExists() {
        long id = 12345L;

        userService.createUserIfUserNotExists(id);

        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    /**
     * Тест создания объекта пользователя если он уже есть
     */
    @Test
    void testCreateUserIfUserExists() {
        long id = 12345L;
        Mockito.when(userRepository.existsById(id)).thenReturn(true);
        userService.createUserIfUserNotExists(id);

        Mockito.verify(userRepository, Mockito.never()).save(ArgumentMatchers.any(User.class));
    }

    /**
     * Тест получения объекта пользователя по ID
     */
    @Test
    void testGetUserById_UserExists() throws UserNotFoundException {
        long id = 12345L;
        User user = new User(id);

        Mockito.when(userRepository.findById(id)).thenReturn(user);

        User result = userService.getUserById(id);

        Assertions.assertEquals(user, result);
        Mockito.verify(userRepository, Mockito.times(1)).findById(id);
    }

    /**
     * Тест получения объекта пользователя по ID, когда такого не существует
     */
    @Test
    void testGetUserById_UserNotFound() {
        long id = 12345L;

        Mockito.when(userRepository.findById(id)).thenReturn(null);

        Exception exception = Assertions.assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(id);
        });

        Assertions.assertEquals(String.format("Пользователь с id %s не найден", id), exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1)).findById(id);
    }
}