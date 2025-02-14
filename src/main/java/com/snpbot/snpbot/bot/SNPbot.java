package com.snpbot.snpbot.bot;

import com.snpbot.snpbot.bot.model.Client;
import com.snpbot.snpbot.bot.service.ClientService;
import com.snpbot.snpbot.bot.utils.WordDocumentCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.UserProfilePhotos;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SNPbot extends TelegramLongPollingBot {

    @Autowired
    ClientService clientService;

    @Autowired
    WordDocumentCreator wordDocumentCreator;
    private int fioInrupOperationNumber = 0;

    private Map<String, String> clientParameters = new HashMap<>();

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
                clientParameters.clear();
                clientParameters.put("telegrammuserid", String.valueOf(update.getMessage().getFrom().getId()));
                if (!update.getMessage().getFrom().getUserName().isEmpty()) {
                    clientParameters.put("username", update.getMessage().getFrom().getUserName());
                }
                getUserProfilePhoto(update.getMessage().getFrom().getId());
                startMessageHandler(update);

            }
            if (fioInrupOperationNumber > 0) {
                switch (fioInrupOperationNumber) {
                    case 1:
                        if (update.getMessage().getText().length() > 1) {
                            clientParameters.put("firstname", update.getMessage().getText());
                            sendMessage(update.getMessage().getChatId(), "Введите фамилию");
                            fioInrupOperationNumber = 2;
                        } else {
                            sendMessage(update.getMessage().getChatId(),
                                    "Имя должно содержать больше одного симвода, введите имя еще раз");
                        }
                        break;
                    case 2:
                        if (update.getMessage().getText().length() > 1) {
                            clientParameters.put("lastname", update.getMessage().getText());
                            sendMessage(update.getMessage().getChatId(), "Введите отчество");
                            fioInrupOperationNumber = 3;
                        } else {
                            sendMessage(update.getMessage().getChatId(),
                                    "Фамилия должна содержать больше одного симвода, введите фамилию еще раз");
                        }
                        break;
                    case 3:
                        if (update.getMessage().getText().length() > 1) {
                            clientParameters.put("middlename", update.getMessage().getText());
                            sendMessage(update.getMessage().getChatId(),
                                    "Введите дату рождения в формате: dd.MM.yyyy");
                            fioInrupOperationNumber = 4;
                        }
                        break;
                    case 4:
                        if (update.getMessage().getText().length() > 1) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                            try {
                                LocalDate.parse(update.getMessage().getText(), formatter);
                                clientParameters.put("birthDate", update.getMessage().getText());

                                SendMessage message = new SendMessage();
                                message.setChatId(update.getMessage().getChatId());
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

                                try {
                                    execute(message);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                fioInrupOperationNumber = 5;
                            } catch (DateTimeParseException e) {
                                sendMessage(update.getMessage().getChatId(),
                                        "Введите дату рождения в формате: dd.MM.yyyy");
                            }
                        }
                        break;
                }
            }
        }
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if ("AGREE".equals(callbackData)) {
                sendMessage(chatId, "Вы согласились на обработку персональных данных ✅");
                fioInrupOperationNumber = 1;
                sendMessage(chatId, "Введите имя");
            }
            if ("TRUE".equals(callbackData)) {
                clientParameters.put("sex", "true");
                fioInrupOperationNumber = 0;
                createClient(update);
            }
            if ("FALSE".equals(callbackData)) {
                clientParameters.put("sex", "false");
                fioInrupOperationNumber = 0;
                createClient(update);
            }
        }

    }

    private void startMessageHandler(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        if (messageText.contains("?")) {
            String[] keyValuePairs = messageText.substring(messageText.indexOf("?") + 1).split("&");
            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    switch (keyValue[0]) {
                        case "utm_source":
                            clientParameters.put("utm_source", keyValue[1]);
                            break;
                        case "utm_medium":
                            clientParameters.put("utm_medium", keyValue[1]);
                            break;
                        case "utm_campaign":
                            clientParameters.put("utm_campaign", keyValue[1]);
                            break;
                    }
                }
            }
        }
        sendInlineKeyboard(chatId);
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

    private void sendInlineKeyboard(Long chatId) {
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

    public void downloadFileById(String fileId, Long userId) {
        try {
            // Получаем объект файла через Telegram API
            org.telegram.telegrambots.meta.api.objects.File file = execute(new GetFile(fileId));

            // Формируем URL для скачивания файла
            String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
            System.out.println("Downloading: " + fileUrl);

            // Скачиваем файл
            InputStream in = new URL(fileUrl).openStream();
            FileOutputStream out = new FileOutputStream("user_photos/" + userId + ".jpg");

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserProfilePhoto(Long userId) {
        GetUserProfilePhotos getUserProfilePhotos = new GetUserProfilePhotos();
        getUserProfilePhotos.setUserId(userId);
        getUserProfilePhotos.setLimit(1);
        try {
            UserProfilePhotos photos = execute(getUserProfilePhotos);
            if (photos.getTotalCount() > 0) {
                String fileId = photos.getPhotos().get(0).get(0).getFileId();
                downloadFileById(fileId, userId);
                clientParameters.put("pathtofoto", "user_photos/" + userId + ".jpg");
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendDocument(long chatId, String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Файл не найден: " + filePath);
            return;
        }

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile(file));

        try {
            execute(sendDocument); // Отправка документа
            System.out.println("Документ отправлен: " + filePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void createClient(Update update) {
        fioInrupOperationNumber = 0;
        Client client = new Client();
        client.setTelegrammUserId(Long.valueOf(clientParameters.get("telegrammuserid")));
        if (clientParameters.containsKey("username")) {
            client.setUsername(clientParameters.get("username"));
        }
        client.setFirstname(clientParameters.get("firstname"));
        client.setLastname(clientParameters.get("lastname"));
        client.setMiddlename(clientParameters.get("middlename"));
        if (!clientParameters.containsKey("utm_source")) {
            client.setUtm_source(clientParameters.get("utm_source"));
        }
        if (!clientParameters.containsKey("utm_medium")) {
            client.setUtm_medium(clientParameters.get("utm_medium"));
        }
        if (!clientParameters.containsKey("utm_campaign")) {
            client.setUtm_campaign(clientParameters.get("utm_campaign"));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        client.setBirthDate(LocalDate.parse(clientParameters.get("birthDate"), formatter));
        client.setSex(Boolean.parseBoolean(clientParameters.get("sex")));
        if (clientParameters.containsKey("pathtofoto")) {
            client.setPathtofoto(clientParameters.get("pathtofoto"));
        }
        wordDocumentCreator.createDocument(client);
        clientService.saveClient(client);
        sendDocument(update.getCallbackQuery().getMessage().getChatId(),
                "docs/" + client.getTelegrammUserId() + ".docx");
    }

}
