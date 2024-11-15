package ru.naumen.model;

/**
 * Состояние пользователя
 */
public enum State {
    /**
     * Первый шаг генерации - ввод длины пароля
     */
    GENERATION_STEP_1,
    /**
     * Второй щаг генерации - ввод сложности пароля
     */
    GENERATION_STEP_2,
    /**
     * Первый шаг сохранения - ввод пароля
     */
    SAVE_STEP_1,
    /**
     * Второй шаг сохранения - ввод описания
     */
    SAVE_STEP_2,
    /**
     * Первый шаг изменения - ввод индекса пароля в списке
     */
    EDIT_STEP_1,
    /**
     * Второй шаг изменения - ввод длины пароля
     */
    EDIT_STEP_2,
    /**
     * Третий шаг изменения - ввод сложности пароля
     */
    EDIT_STEP_3,
    /**
     * Четвертый шаг изменения, ввод описания пароля
     */
    EDIT_STEP_4,
    /**
     * Шаг для удаления - ввод индекса
     */
    DELETE_STEP_1,
    /**
     * Шаг для сортировки - ввод параметра сортировки
     */
    SORT_STEP_1,
    /**
     * Шаг для поиска - ввод запроса
     */
    FIND_STEP_1,
    /**
     * Пользователь находится в списке паролей
     */
    IN_LIST,
    /**
     * Пользователь не находится ни в одной команде
     */
    NONE,
}
