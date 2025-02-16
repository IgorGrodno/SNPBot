package com.snpbot.snpbot.bot.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class LayoutsManager {
    public SendMessage getInlineKeyboard(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы даете согласие на обработку персональных данных?");

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

        return message;
    }

    public SendMessage getSexChoiseButtons(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Пожалуйста, выберите пол:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton trueButton = new InlineKeyboardButton();
        trueButton.setText("Мужской");
        trueButton.setCallbackData("TRUE");

        InlineKeyboardButton falseButton = new InlineKeyboardButton();
        falseButton.setText("Женский");
        falseButton.setCallbackData("FALSE");

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(trueButton);
        rowInline.add(falseButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        return message;
    }
}
