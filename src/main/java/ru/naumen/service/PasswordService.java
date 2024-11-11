package ru.naumen.service;

import org.springframework.transaction.annotation.Transactional;
import ru.naumen.exception.UserNotFoundException;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.repository.UserPasswordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

/**
 * Класс для работы с паролями
 */
@Service
public class PasswordService {

    private final SecureRandom random = new SecureRandom();
    private final EncodeService encodeService;
    private final UserService userService;
    private final UserPasswordRepository userPasswordRepository;
    private final Logger log = LoggerFactory.getLogger(PasswordService.class);


    public PasswordService(EncodeService encodeService, UserService userService, UserPasswordRepository userPasswordRepository) {
        this.encodeService = encodeService;
        this.userService = userService;
        this.userPasswordRepository = userPasswordRepository;
    }

    /**
     * Создаёт пароль и сохраняет в БД
     *
     * @param password пароль
     * @param description описание пароля
     * @param userId ID пользователя
     */
    @Transactional
    public void createUserPassword(String password, String description, long userId) {
        try {
            String encodedPassword = encodeService.encryptData(password);
            User user = userService.getUserByTelegramId(userId);
            UserPassword userPassword = new UserPassword(description, encodedPassword, user);

            userPasswordRepository.save(userPassword);
            log.info("Создан новый пароль {}", userPassword.getUuid());
        } catch (UserNotFoundException e) {
            log.error("Ошибка при создании пароля - не найден пользователь", e);
        }
    }

    /**
     * Возвращает список паролей конкретного пользователя
     *
     * @param userId ID пользователя
     */
    @Transactional(readOnly = true)
    public List<UserPassword> getUserPasswords(long userId) {
        return userPasswordRepository.findByUserTelegramId(userId);
    }

    /**
     * Удаляет пароль
     *
     * @param uuid uuid
     */
    @Transactional
    public void deletePassword(String uuid) {
        if (userPasswordRepository.existsByUuid(uuid)) {
            userPasswordRepository.deleteByUuid(uuid);
            log.info("Удалён пароль {}", uuid);
        }
    }

    /**
     * Обновляет данные для пароля
     * @param uuid uuid
     * @param description описание (если передаётся null, то не обновляется)
     * @param password пароль
     */
    public void updatePassword(String uuid, String description, String password) {
        if (userPasswordRepository.existsByUuid(uuid)) {
            UserPassword userPassword = userPasswordRepository.findByUuid(uuid);

            String encodedPassword = encodeService.encryptData(password);
            userPassword.setPassword(encodedPassword);
            if (description != null) {
                userPassword.setDescription(description);
            }

            userPasswordRepository.save(userPassword);
            log.info("Обновлён пароль {}", uuid);
        }
    }

    /**
     * Ищет пароль по UUID
     * @param uuid uuid
     * @return найденный пароль
     */
    public UserPassword findPasswordByUuid(String uuid) {
        UserPassword password = userPasswordRepository.findByUuid(uuid);
        if (password == null) {
            throw new IllegalArgumentException("Пароль не найден!");
        }
        return password;
    }

    /**
     * Генерирует пароль заданной длины и сложности и проверяет его соответствие.
     *
     * @param length     длина пароля
     * @param complexity сложность пароля
     * @return сгенерированный пароль
     */
    public String generatePasswordWithComplexity(int length, int complexity) {
        String chars = getCharsForPassword(complexity);
        boolean passwordMatch = false;
        String password = "";
        while (!passwordMatch) {
            password = generatePassword(length, chars);
            passwordMatch = matchPassword(complexity, password);
        }

        log.info("Сгенерирован пароль");
        return password;
    }

    /**
     * Проверяет на соответствия пароля заданной сложности
     * @param complexity - сложность пароля (от 1 до 3)
     * @param password - пароль
     * @return true - в случае соответствия, false - в случае не соответствия, ошибку - при неверном значении сложности
     */
    private boolean matchPassword(int complexity, String password){
        return switch (complexity) {
            case 1 -> password.matches("^[a-z]+$");
            case 2 -> password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]+$");
            case 3 -> password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+<>?])[a-zA-Z\\d!@#$%^&*()\\-_=+<>?]+$");
            default -> throw new IllegalArgumentException("Unexpected value: " + complexity);
        };
    }

    /**
     * Метод генерации пароля
     *
     * @param length - длина пароля
     * @param chars - допустимые символы для пароля
     * @return сгенерированный пароль
     */
    private String generatePassword(int length, String chars) {
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    /**
     * Определяет набор символов в зависимости от сложности.
     *
     * @param complexity сложность пароля
     * @return строка символов для использования в пароле
     */
    private String getCharsForPassword(int complexity) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String specialChars = "!@#$%^&*()-_=+<>?";

        return switch (complexity) {
            case 1 -> lowercase;
            case 2 -> lowercase + uppercase + digits;
            case 3 -> lowercase + uppercase + digits + specialChars;
            default -> throw new IllegalArgumentException("Unexpected value: " + complexity);
        };
    }
}