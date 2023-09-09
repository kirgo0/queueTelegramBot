package main.java.com.ffc.bot.strategy.textStrategy.method;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.strategy.CallbackData;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class GetInfoMethod implements StrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(i+1));
            StringBuilder sb = new StringBuilder();
            sb.append(CallbackData.GetInfo).append(i);
            button.setCallbackData(sb.toString());
            buttons.add(button);
        }

        response.setParseMode("HTML");
        response.setText(MongoDB.getInfo(0));

        keyboard.add(buttons);

        response.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
        return List.of(response);
    }
}
