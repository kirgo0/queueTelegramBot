package main.java.com.ffc.bot.strategy.textStrategy.method;

import main.java.com.ffc.bot.InfoMarkupConstructor;
import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.strategy.CallbackData;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class GetInfoMethod implements TextStrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {

        response.setParseMode("HTML");
        response.setText(MongoDB.getInfo(0));

        response.setReplyMarkup(InfoMarkupConstructor.getMarkup());
        return List.of(response);
    }
}
