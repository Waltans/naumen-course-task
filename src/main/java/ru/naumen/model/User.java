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

    /**
     * Кодовое слово пользователя
     */
    private String codePhrase = null;

    public String getCodePhrase() {
        return codePhrase;
    }

    public void setCodePhrase(String codePhrase) {
        this.codePhrase = codePhrase;
    }

    public User(long id, List<UserPassword> userPasswords) {
        this.id = id;
        this.userPasswords = userPasswords;
    }

    public User() {

    }

    public User(long id) {
        this.id = id;
    }

    public List<UserPassword> getPasswords() {
        return userPasswords;
    }

    public void setPasswords(UserPassword userPassword) {
        this.userPasswords.add(userPassword);
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