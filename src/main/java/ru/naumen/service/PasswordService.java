package ru.naumen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.naumen.exception.*;
import ru.naumen.handler.validators.PasswordValidator;
import ru.naumen.model.User;
import ru.naumen.model.UserPassword;
import ru.naumen.repository.UserPasswordRepository;

import java.security.SecureRandom;
import java.util.List;

import static ru.naumen.bot.constants.Parameters.*;

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
     * @param password    пароль
     * @param description описание пароля
     * @param userId      ID пользователя
     */
    @Transactional
    public void createUserPassword(String password, String description, long userId)
            throws UserNotFoundException, EncryptException {
        String encodedPassword = encodeService.encryptData(password);
        User user = userService.getUserById(userId);
        UserPassword userPassword = new UserPassword(description, encodedPassword, user);

        userPasswordRepository.save(userPassword);
        log.info("Создан новый пароль {}", userPassword.getUuid());
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
     * Ищет пароли у конкретного пользователя
     *
     * @param userId        ID пользователя
     * @param searchRequest поисковый запрос пароля (частичное описание без учёта регистра)
     */
    @Transactional(readOnly = true)
    public List<UserPassword> getUserPasswordsWithPartialDescription(long userId, String searchRequest) {
        return userPasswordRepository.findByDescriptionContainsIgnoreCaseAndUserId(searchRequest, userId);
    }

    /**
     * Ищет пароли у конкретного пользователя
     *
     * @param userId ID пользователя
     * @return список с отсортированными паролями или пустой список, если паролей у пользователя нет
     */
    @Transactional(readOnly = true)
    public List<UserPassword> getUserPasswordsSorted(long userId, SortType sortType) throws IncorrectSortTypeException {
        switch (sortType) {
            case BY_DATE -> {
                return userPasswordRepository.findByUserIdOrderByLastModifyDate(userId);
            }
            case BY_DESCRIPTION -> {
                return userPasswordRepository.findByUserIdOrderByDescriptionAsc(userId);
            }
            default -> throw new IncorrectSortTypeException("Некорректный тип сортировки!");
        }
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
     *
     * @param uuid        uuid
     * @param description описание (если передаётся null, то не обновляется)
     * @param password    пароль
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
     *
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
     *
     * @param userId id пользователя
     */
    public int countPasswordsByUserId(long userId) {
        return userPasswordRepository.countByUserId(userId);
    }

    /**
     * Генерирует пароль по заданным параметрам
     *
     * @param complexity сложность
     * @param length     длина
     * @return пароль
     */
    public String generatePassword(int length, String complexity) throws PasswordLengthException, ComplexityFormatException {
        PasswordValidator passwordValidator = new PasswordValidator();
        int complexityValue = parseComplexity(complexity, passwordValidator);

        if (!passwordValidator.isValidLength(length)) {
            throw new PasswordLengthException("Password length should be between 8 and 128");
        }

        StringBuilder password = new StringBuilder(length);
        String characterSet = LOWERCASE;

        if (complexityValue >= 2) {
            characterSet += DIGITS + UPPERCASE;
        }
        if (complexityValue == 3) {
            characterSet += SPECIAL_CHARACTERS;
        }

        if (complexityValue >= 2) {
            password.append(getRandomCharacter(DIGITS));
            password.append(getRandomCharacter(UPPERCASE));
            password.append(getRandomCharacter(LOWERCASE));
        }
        if (complexityValue == 3) {
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
     *
     * @param characters набор символов
     */
    private char getRandomCharacter(String characters) {
        return characters.charAt(random.nextInt(characters.length()));
    }

    /**
     * Проверяет валидность индекса пароля
     *
     * @param passwordIndex - индекс
     * @param userId        - ID пользователя
     * @return true, если индекс валиден
     */
    public boolean isValidPasswordIndex(int passwordIndex, long userId) {
        int countPasswords = getUserPasswords(userId).size();

        return (passwordIndex <= countPasswords) && (passwordIndex >= 1);
    }

    /**
     * Метод парсит сложность в число
     *
     * @param complexity        - сложность в виде строки
     * @param passwordValidator - валидатор
     * @return - число обозначающее сложность или ошибку
     * @throws ComplexityFormatException - ошибка, что число не может быть конвертировано
     */
    private int parseComplexity(String complexity, PasswordValidator passwordValidator)
            throws ComplexityFormatException {

        if (!passwordValidator.isValidComplexity(complexity)) {
            throw new ComplexityFormatException("Complexity should be between 1 and 3");
        }

        return switch (complexity) {
            case "1", COMPLEXITY_EASY -> 1;
            case "2", COMPLEXITY_MEDIUM -> 2;
            case "3", COMPLEXITY_HARD -> 3;
            default -> throw new IllegalArgumentException("Некорректно задана сложность!");
        };
    }
}