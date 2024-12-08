package ru.naumen.model;

import jakarta.persistence.*;
import ru.naumen.exception.UserCodePhraseException;

import java.time.LocalDate;
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
    @Column(name = "id", nullable = false)
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

    /**
     * День смены кодового слова
     */
    private LocalDate codeModifyDate;

    public void setCodeModifyDate() {
        this.codeModifyDate = LocalDate.now();
    }

    public LocalDate getCodeModifyDate() {
        return codeModifyDate;
    }

    public String getCodePhrase() {
        return codePhrase;
    }

    /**
     * Устанавливает кодовое слово, если оно ещё не было установлено или если прошло 30 дней с момента установки
     *
     * @param codePhrase - кодовое слово
     * @throws UserCodePhraseException - ошибка, в случае, если невозможно поменять кодовое слово
     */
    public void setCodePhrase(String codePhrase) throws UserCodePhraseException {
        if (getCodePhrase() == null ||
                getCodeModifyDate().isBefore(LocalDate.now().minusDays(30))) {
            setCodeModifyDate();
            this.codePhrase = codePhrase;
        }
        else {
            throw new UserCodePhraseException("Невозможно сменить кодовое слово");
        }
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

    public long getId() {
        return id;
    }

    public void setId(long telegramId) {
        this.id = telegramId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}