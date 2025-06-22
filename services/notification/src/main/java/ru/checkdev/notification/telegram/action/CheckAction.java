package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonTgDTO;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class CheckAction implements Action {
    private final TgAuthCallWebClint authCallWebClint;
    private static final String URI_GET_BY_TG = "/person/tg/";

    @Override
    public BotApiMethod<Message> handle(Message message) {
        String chatId = message.getChatId().toString();
        Long tgId = message.getFrom().getId();
        String text = "";
        String sl = System.lineSeparator();
        try {
            Optional<PersonTgDTO> personOptional = authCallWebClint.doGet(URI_GET_BY_TG + tgId, PersonTgDTO.class).blockOptional();
            if (personOptional.isEmpty()) {
                text = "Пользователь, привязанный к данному telegram, не найден.";
            } else {
                PersonTgDTO person = personOptional.get();
                String name = person.getUsername() == null ? "(не задано)" : person.getUsername();
                text = String.format("Пользователь, привязанный к данному telegram: Email - %s, Username - %s",
                        person.getEmail(), name);
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
        return new SendMessage(chatId, TgConfig.START_MESSAGE);
    }
}
