package ru.checkdev.notification.domain;

import lombok.*;

import java.util.Calendar;

@Data
@NoArgsConstructor
public class PersonTgDTO {

    private String email;
    private String password;
    private String username;
    private boolean privacy;
    private long telegramId;
    private Calendar created;

    public PersonTgDTO(String email, String password, boolean privacy, long telegramId, Calendar created) {
        this.email = email;
        this.password = password;
        this.privacy = privacy;
        this.telegramId = telegramId;
        this.created = created;
    }

    public PersonTgDTO(String email, long telegramId) {
        this.email = email;
        this.telegramId = telegramId;
    }
}
