package com.snpbot.snpbot.bot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
@Configuration
public class BotConfig  {

    @Bean
    public TelegramBotsApi telegramBotsApi(SNPbot snpBot) throws Exception {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(snpBot);
        return telegramBotsApi;
    }

}