package ru.naumen.handler.validators;

import static ru.naumen.bot.constants.Parameters.*;

/**
 * Класс для унификации валидации пароля
 */
public class PasswordValidator {


    /**
     * Проверяем корректность введённой длины пароля
     *
     * @param length - длина пароля
     * @return корректна ли длина
     */
    public boolean isValidLength(int length) {
        return length >= MINIMUM_PASSWORD_LENGTH && length <= MAXIMUM_PASSWORD_LENGTH;
    }

    /**
     * Метод проверяет что указана правильная сложность (от 1 до 3)
     *
     * @param complexity - сложность пароля
     * @return корректна ли сложность
     */
    public boolean isValidComplexity(String complexity) {
        return complexity.equals("1")
                || complexity.equals("2")
                || complexity.equals("3")
                || complexity.equals(COMPLEXITY_EASY)
                || complexity.equals(COMPLEXITY_MEDIUM)
                || complexity.equals(COMPLEXITY_HARD);
    }
}
