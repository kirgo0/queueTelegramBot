package main.java.com.ffc.bot.strategy;

import main.java.com.ffc.bot.specialMessage.ChatJoinMessage;
import main.java.com.ffc.bot.strategy.textStrategy.method.CheckAuthoriseMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class ChatJoinStrategy implements Strategy {
    @Override
    public List<BotApiMethod> getResponse(Update update) {
        if(!update.getMyChatMember().getOldChatMember().getStatus().equalsIgnoreCase("member")) {

            List<BotApiMethod> methods = new ArrayList<>();

            String chatId = String.valueOf(update.getMyChatMember().getChat().getId());

            ChatJoinMessage response = new ChatJoinMessage();

            response.setChatId(chatId);
            response.setText("Привіт! Я чат бот ezQueue, який допоможе вам створити живу чергу прямо в телеграмі.\nДля отримання додаткової інформації використовуйте команду /getInfo");

            methods.add(response);
            methods.addAll(new CheckAuthoriseMethod().getResponse(update,null,chatId));
            return methods;
        }
        return null;
    }
}
