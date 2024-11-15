package ru.naumen.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс пароля
 */
@Entity
@Table(name = "tbl_passwords")
public class UserPassword {

    /**
     * Уникальный идентификатор пароля, UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    /**
     * Описание пароля
     */
    @Column(name = "description", nullable = true, unique = false)
    private String description;

    /**
     * Пароль в зашифрованном виде
     */
    @Column(name = "password", nullable = false, unique = false)
    private String password;

    /**
     * Пользователь, которому принадлежит пароль
     */
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    private User user;

    /**
     * Дата последнего изменения пароля
     */
    @Column(name = "date", nullable = false, unique = false)
    private LocalDate lastModifyDate;

    public UserPassword(String uuid, String description, String password, User user, LocalDate lastModifyDate) {
        this.uuid = uuid;
        this.description = description;
        this.password = password;
        this.user = user;
        this.lastModifyDate = lastModifyDate;
    }

    public UserPassword() {

    }

    public UserPassword(String description, String password, User user) {
        this.description = description;
        this.password = password;
        this.user = user;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDescription() {
        return description;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getLastModifyDate() {
        return lastModifyDate;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Устанавливает дату последнего обновления пароля при его сохранении в базу данных
     */
    @PrePersist
    @PreUpdate
    private void setLastModifyDate() {
        this.lastModifyDate = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPassword that = (UserPassword) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(description, that.description) && Objects.equals(password, that.password) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, description, password, user);
    }
}