package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonTgDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckActionTest {

    @Mock
    private TgAuthCallWebClint authCallWebClint;
    @Mock
    private Message message;
    @Mock
    private User user;
    private CheckAction checkAction;

    @BeforeEach
    void setUp() {
        checkAction = new CheckAction(authCallWebClint);
    }

    @Test
    void whenUserNotFoundThenReturnNotFoundMessage() {
        when(message.getChatId()).thenReturn(123L);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(456L);
        when(authCallWebClint.doGet(anyString(), any()))
                .thenReturn(Mono.justOrEmpty(Optional.empty()));

        SendMessage result = (SendMessage) checkAction.handle(message);

        assertEquals("123", result.getChatId());
        assertEquals("Пользователь, привязанный к данному telegram, не найден.", result.getText());
    }

    @Test
    void whenUserFoundThenReturnUserInfo() {
        when(message.getChatId()).thenReturn(123L);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(456L);

        PersonTgDTO person = new PersonTgDTO();
        person.setEmail("test@example.com");
        person.setUsername("user");
        when(authCallWebClint.doGet(anyString(), any()))
                .thenReturn(Mono.justOrEmpty(Optional.of(person)));

        SendMessage result = (SendMessage) checkAction.handle(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("test@example.com"));
        assertTrue(result.getText().contains("user"));
    }

    @Test
    void whenServiceErrorThenReturnErrorMessage() {
        when(message.getChatId()).thenReturn(123L);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(456L);
        when(authCallWebClint.doGet(anyString(), any()))
                .thenThrow(new RuntimeException());

        SendMessage result = (SendMessage) checkAction.handle(message);

        assertEquals("123", result.getChatId());
        assertEquals(TgConfig.ERROR_SERVICE_MESSAGE, result.getText());
    }
}