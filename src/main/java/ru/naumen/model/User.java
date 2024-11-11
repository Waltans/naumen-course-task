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
     * Уникальный идентификатор пользователя, UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    /**
     * Имя пользователя
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * Идентификатор пользователя в Telegram
     */
    @Column(name = "telegram_id", nullable = false)
    private long telegramId;

    /**
     * Список паролей пользователя
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPassword> userPasswords;

    public User(String uuid, String username, long telegramId, List<UserPassword> userPasswords) {
        this.uuid = uuid;
        this.username = username;
        this.telegramId = telegramId;
        this.userPasswords = userPasswords;
    }

    public User() {

    }

    public User(String username, long telegramId) {
        this.username = username;
        this.telegramId = telegramId;
    }

    public String getUuid() {
        return uuid;
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

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return telegramId == user.telegramId && Objects.equals(uuid, user.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, telegramId);
    }
}