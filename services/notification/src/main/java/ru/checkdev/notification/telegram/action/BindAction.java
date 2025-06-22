package ru.checkdev.notification.telegram.action;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonTgDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class BindAction implements Action {
    private static final String URI_GET_BY_TG = "/person/tg/";
    private static final String URI_SET_TG = "/person/changeTelegram";

    @NonNull
    private TgAuthCallWebClint authCallWebClint;
    private boolean attached;

    @Override
    public BotApiMethod<Message> handle(Message message) {
        String chatId = message.getChatId().toString();
        var sl = System.lineSeparator();
        String text = "";
        try {
            Optional<PersonTgDTO> personOptional = authCallWebClint.doGet(URI_GET_BY_TG + message.getFrom().getId(),
                    PersonTgDTO.class).blockOptional();
            if (personOptional.isPresent()) {
                text = "Данный аккаунт уже привязан к CheckDev" + sl + TgConfig.START_MESSAGE;
                attached = true;
            } else {
                text = "Введите email и пароль через пробел для привязки " + sl + "(пример: ivan@mail.ru 12345): ";
                attached = false;
            }
            return new SendMessage(chatId, text);
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            return new SendMessage(chatId, TgConfig.ERROR_SERVICE_MESSAGE);
        }
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        String chatId = message.getChatId().toString();
        if (attached) {
            return new SendMessage(chatId, TgConfig.START_MESSAGE);
        }
        Long tgId = message.getFrom().getId();
        String[] response = message.getText().trim().split(" ");
        String email = response[0];
        String password = response[1];
        String sl = System.lineSeparator();
        String text = "";
        try {
            String token = authCallWebClint.authorize(email, password).block();
            if (!token.isEmpty()) {
                text = authCallWebClint.doPut(URI_SET_TG, new PersonTgDTO(email, tgId), token).block() == HttpStatus.OK
                        ? "Аккаунт успешно привязан"
                        : "Ошибка привязки аккаунта";
            } else {
                text = "Неверный логин или пароль";
            }
            return new SendMessage(chatId, text);
        } catch (Exception e) {
            log.error("WebClient doPut error: {}", e.getMessage());
            return new SendMessage(chatId, TgConfig.ERROR_SERVICE_MESSAGE);
        }
    }
}
