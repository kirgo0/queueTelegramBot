package main.java.com.ffc.bot.queueHandler;

import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.MongoDB;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class QueueMarkupConstructor {

    public static InlineKeyboardMarkup getMarkup(String chatId) {

        JSONArray obj = new JSONArray(MongoDB.getQueue(chatId));

        List<List<InlineKeyboardButton>> inline_keyboard = new ArrayList<>();

        for (int i = 0; i < obj.length(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(". ");
            sb.append(obj.getString(i).equals("_") ? " _______________________ " : MongoDB.getUserName(obj.getString(i).trim()));
            button.setText(sb.toString());
            sb = new StringBuilder();

            sb.append(obj.getString(i).equals("_") ? CallbackData.QueueIn.toString() : CallbackData.QueueOut.toString());
            sb.append(i);
            button.setCallbackData(sb.toString());
            inline_keyboard.add(List.of(button));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(inline_keyboard);
        return markup;
    }
}
