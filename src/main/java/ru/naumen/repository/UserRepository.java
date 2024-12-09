package ru.naumen.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.naumen.model.User;

/**
 * Репозиторий с пользователями
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Проверяет, существует ли пользователь по Id
     *
     * @param id Id пользователя
     */
    boolean existsById(long id);

    /**
     * Находит пользователя с указанным Id
     *
     * @param id Id пользователя
     */
    User findById(long id);
}