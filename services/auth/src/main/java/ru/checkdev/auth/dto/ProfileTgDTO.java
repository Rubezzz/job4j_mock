package ru.checkdev.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfileTgDTO {
    @EqualsAndHashCode.Include
    private Integer id;
    private String username;
    private String email;
    private long telegramId;

    public ProfileTgDTO(Integer id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}