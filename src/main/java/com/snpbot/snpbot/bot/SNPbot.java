package com.snpbot.snpbot.bot;

import com.snpbot.snpbot.bot.service.ClientService;
import com.snpbot.snpbot.bot.utils.LayoutsManager;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SNPbot extends TelegramLongPollingBot {

    @Autowired
    ClientService clientService;

    @Autowired
    WordDocumentCreator wordDocumentCreator;

    @Autowired
    LayoutsManager layoutsManager;
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
                            sendMessage(
                                    update.getMessage().getChatId(),
                                    "Фамилия должна содержать больше одного симвода, введите фамилию еще раз"
                            );
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
                        if (update.getMessage().getText().length() == 10) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                            try {
                                LocalDate.parse(update.getMessage().getText(), formatter);
                                clientParameters.put("birthDate", update.getMessage().getText());
                                sendMessage(layoutsManager.getSexChoiseButtons(update.getMessage().getChatId()));
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
                sendDocument(chatId, wordDocumentCreator.createDocument(clientService.createClient(clientParameters)));
            }
            if ("FALSE".equals(callbackData)) {
                clientParameters.put("sex", "false");
                fioInrupOperationNumber = 0;
                sendDocument(chatId, wordDocumentCreator.createDocument(clientService.createClient(clientParameters)));
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
        sendMessage(layoutsManager.getInlineKeyboard(chatId));
    }

    public void getUserProfilePhoto(Long userId) {
        GetUserProfilePhotos getUserProfilePhotos = new GetUserProfilePhotos();
        getUserProfilePhotos.setUserId(userId);
        getUserProfilePhotos.setLimit(1);
        try {
            UserProfilePhotos photos = execute(getUserProfilePhotos);
            if (photos.getTotalCount() > 0) {
                String fileId = photos.getPhotos().get(0).get(0).getFileId();
                org.telegram.telegrambots.meta.api.objects.File file = execute(new GetFile(fileId));
                String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
                System.out.println("Downloading: " + fileUrl);
                InputStream in = new URL(fileUrl).openStream();
                FileOutputStream out = new FileOutputStream("user_photos/" + userId + ".jpg");
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                in.close();
                clientParameters.put("pathtofoto", "user_photos/" + userId + ".jpg");
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(SendMessage message) {
        try {
            execute(message);
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
            execute(sendDocument);
            System.out.println("Документ отправлен: " + filePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
