package ru.naumen.service;

import org.springframework.transaction.annotation.Transactional;
import ru.naumen.exception.PasswordNotFoundException;
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
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+<>";


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
            User user = userService.getUserById(userId);
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
        return userPasswordRepository.findByUserId(userId);
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
    public UserPassword findPasswordByUuid(String uuid) throws PasswordNotFoundException {
        UserPassword password = userPasswordRepository.findByUuid(uuid);
        if (password == null) {
            throw new PasswordNotFoundException("Пароль не найден!");
        }
        return password;
    }

    /**
     * Подсчитывает количество паролей пользователя
     * @param userId id пользователя
     */
    public int countPasswordsByUserId(long userId) {
        return userPasswordRepository.countByUserId(userId);
    }

    /**
     * Генерирует пароль по заданным параметрам
     * @param complexity сложность
     * @param length длина
     * @return пароль
     */
    public String generatePassword(int length, int complexity) {
        StringBuilder password = new StringBuilder(length);
        String characterSet = LOWERCASE;

        if (complexity >= 2) {
            characterSet += DIGITS + UPPERCASE;
        }
        if (complexity == 3) {
            characterSet += SPECIAL_CHARACTERS;
        }

        if (complexity >= 2) {
            password.append(getRandomCharacter(DIGITS));
            password.append(getRandomCharacter(UPPERCASE));
            password.append(getRandomCharacter(LOWERCASE));
        }
        if (complexity == 3) {
            password.append(getRandomCharacter(DIGITS));
            password.append(getRandomCharacter(SPECIAL_CHARACTERS));
            password.append(getRandomCharacter(UPPERCASE));
            password.append(getRandomCharacter(LOWERCASE));
        }

        while (password.length() < length) {
            password.append(getRandomCharacter(characterSet));
        }

        return password.toString();
    }

    /**
     * Получает случайный символ из набора
     * @param characters набор символов
     */
    private char getRandomCharacter(String characters) {
        return characters.charAt(random.nextInt(characters.length()));
    }
}