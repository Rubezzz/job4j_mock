package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonTgDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnbindActionTest {

    @Mock
    private TgAuthCallWebClint authCallWebClint;
    @Mock
    private Message message;
    @Mock
    private User user;
    private UnbindAction unbindAction;

    @BeforeEach
    void setUp() {
        unbindAction = new UnbindAction(authCallWebClint);
    }

    @Test
    void whenAccountNotAttachedThenReturnNotAttachedMessage() {


        when(message.getChatId()).thenReturn(123L);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(456L);
        when(authCallWebClint.doGet(anyString(), any()))
                .thenReturn(Mono.justOrEmpty(Optional.empty()));

        SendMessage result = (SendMessage) unbindAction.handle(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("Данный аккаунт не привязан"));
    }

    @Test
    void whenAccountAttachedThenPromptForCredentials() {
        when(message.getChatId()).thenReturn(123L);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(456L);
        when(authCallWebClint.doGet(anyString(), any()))
                .thenReturn(Mono.justOrEmpty(Optional.of(new PersonTgDTO())));

        SendMessage result = (SendMessage) unbindAction.handle(message);

        assertEquals("123", result.getChatId());
        assertTrue(result.getText().contains("Введите email и пароль"));
    }

    @Test
    void whenValidCredentialsThenSuccessfullyUnbind() {
        when(message.getChatId()).thenReturn(123L);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(456L);
        when(authCallWebClint.doGet(anyString(), any()))
                .thenReturn(Mono.just(Optional.of(new PersonTgDTO())));
        unbindAction.handle(message);
        when(message.getText()).thenReturn("test@mail.ru password123");
        when(authCallWebClint.authorize(anyString(), anyString()))
                .thenReturn(Mono.just("valid_token"));
        when(authCallWebClint.doPut(anyString(), any(), anyString()))
                .thenReturn(Mono.just(HttpStatus.OK));

        SendMessage result = (SendMessage) unbindAction.callback(message);

        assertEquals("123", result.getChatId());
        assertEquals("Аккаунт успешно отвязан", result.getText());
    }

    @Test
    void whenServiceErrorThenReturnErrorMessage() {
        when(message.getChatId()).thenReturn(123L);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(456L);
        when(authCallWebClint.doGet(anyString(), any()))
                .thenThrow(new RuntimeException("Service error"));

        SendMessage result = (SendMessage) unbindAction.handle(message);

        assertEquals("123", result.getChatId());
        assertEquals(TgConfig.ERROR_SERVICE_MESSAGE, result.getText());
    }
}