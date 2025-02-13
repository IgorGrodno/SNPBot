package com.snpbot.snpbot.bot;

import com.snpbot.snpbot.bot.model.Client;
import com.snpbot.snpbot.bot.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SNPbot extends TelegramLongPollingBot {

    @Autowired
    ClientService clientService;

    private final Map<Long, Integer> userSteps = new HashMap<>();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().startsWith("/start")) {
                startMessageHandler(update);
            }
        }
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if ("AGREE".equals(callbackData)) {
                sendMessage(chatId, "Вы согласились на обработку персональных данных ✅");

            }
        }

    }

    private void startMessageHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        Client client = new Client();
        if (messageText.contains("?")) {
            String[] keyValuePairs = messageText.substring(messageText.indexOf("?") + 1).split("&");
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    switch (keyValue[0]) {
                        case "utm_source":
                            client.setUtm_source(keyValue[1]);
                            break;
                        case "utm_medium":
                            client.setUtm_medium(keyValue[1]);
                            break;
                        case "utm_campaign":
                            client.setUtm_campaign(keyValue[1]);
                            break;
                    }
                }
            }
        }
        sendInlineKeyboard(chatId, client);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendInlineKeyboard(Long chatId, Client client) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Пожалуйста, выберите действие:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton websiteButton = new InlineKeyboardButton();
        websiteButton.setText("Перейти на сайт");
        websiteButton.setUrl("https://github.com/");

        InlineKeyboardButton agreeButton = new InlineKeyboardButton();
        agreeButton.setText("Согласен ✅");
        agreeButton.setCallbackData("AGREE");

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(websiteButton);
        rowInline.add(agreeButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
