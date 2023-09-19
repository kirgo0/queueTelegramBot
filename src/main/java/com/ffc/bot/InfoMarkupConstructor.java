package main.java.com.ffc.bot;

import main.java.com.ffc.bot.strategy.CallbackData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InfoMarkupConstructor {

    public static InlineKeyboardMarkup getMarkup() {

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

        keyboard.add(buttons);

        return new InlineKeyboardMarkup(keyboard);
    }
}
