package ru.naumen.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

/**
 * Основной класс с пользователями бота
 */
@Entity
@Table(name = "tbl_users")
public class User {

    /**
     * Имя пользователя
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * Идентификатор пользователя
     */
    @Id
    @Column(name = "telegram_id", nullable = false)
    private long id;

    /**
     * Список паролей пользователя
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPassword> userPasswords;

    public User(String username, long id, List<UserPassword> userPasswords) {
        this.username = username;
        this.id = id;
        this.userPasswords = userPasswords;
    }

    public User() {

    }

    public User(String username, long id) {
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public List<UserPassword> getPasswords() {
        return userPasswords;
    }

    public void setPasswords(UserPassword userPassword) {
        this.userPasswords.add(userPassword);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long telegramId) {
        this.id = telegramId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}