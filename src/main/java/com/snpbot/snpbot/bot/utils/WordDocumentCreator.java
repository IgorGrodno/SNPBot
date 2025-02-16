package com.snpbot.snpbot.bot.utils;

import com.snpbot.snpbot.bot.model.Client;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class WordDocumentCreator {
    public String createDocument(Client client) {
        File directory = new File("docs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String filePath = "docs/" + client.getTelegrammUserId() + ".docx";

        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(filePath)) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("Имя пользователя: " + client.getUsername());
            run.addBreak();
            run.setText("Имя: " + client.getFirstname());
            run.addBreak();
            run.setText("Фамилия: " + client.getLastname());
            run.setBold(true);
            run.setFontSize(14);
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }
}
