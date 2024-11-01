package naumenproject.naumenproject.service;

import org.springframework.transaction.annotation.Transactional;
import naumenproject.naumenproject.model.UserPassword;
import naumenproject.naumenproject.repository.UserPasswordRepository;
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

    private final SecureRandom RANDOM = new SecureRandom();
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
     * @param userTelegramId ID пользователя в telegram
     */
    @Transactional
    public void createUserPassword(String password, String description, long userTelegramId) {
        String encodedPassword = encodeService.encryptData(password);
        UserPassword userPassword = new UserPassword();
        userPassword.setDescription(description);
        userPassword.setPassword(encodedPassword);
        userPassword.setUser(userService.getUserByTelegramId(userTelegramId));

        userPasswordRepository.save(userPassword);
        log.info("Создан новый пароль {}", userPassword.getUuid());
    }

    /**
     * Возвращает список паролей конкретного пользователя
     *
     * @param userTelegramId ID пользователя в telegram
     */
    @Transactional(readOnly = true)
    public List<UserPassword> getUserPasswords(long userTelegramId) {
        return userPasswordRepository.findByUserTelegramId(userTelegramId);
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
     * Генерирует пароль заданной длины и сложности.
     *
     * @param length     длина пароля
     * @param complexity сложность пароля
     * @return сгенерированный пароль
     */
    public String generatePassword(int length, int complexity) {
        String chars = getCharsForPassword(complexity);
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        log.info("Сгенерирован пароль");
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
            default -> throw new IllegalStateException("Unexpected value: " + complexity);
        };
    }
}
