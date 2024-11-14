package ru.naumen;

import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.User;
import ru.naumen.repository.UserRepository;
import ru.naumen.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

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
        String name = "TestUser";

        userService.createUserIfUserNotExists(id, name);

        Mockito.verify(userRepository, Mockito.times(1)).save(ArgumentMatchers.any(User.class));
    }

    /**
     * Тест получения объекта пользователя по Telegram ID
     */
    @Test
    void testGetUserById_UserExists() throws UserNotFoundException {
        long id = 12345L;
        User user = new User();
        user.setId(id);
        user.setUsername("TestUser");

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
