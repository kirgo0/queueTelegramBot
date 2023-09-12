package main.java.com.ffc.bot.queueHandler;

import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.MongoDB;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class QueueMarkupConstructor {

    public static InlineKeyboardMarkup getMarkup(String chatId) {

        JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

        if(!MongoDB.getQueueView(chatId)) {
            return smallQueueConstructor(queue);
        } else {
            if(queue.length() <= 15) {
                return smallQueueConstructor(queue);
            } else {
                return largeQueueConstructor(queue);
            }
        }
    }

    private static InlineKeyboardMarkup smallQueueConstructor(JSONArray queue) {

        List<List<InlineKeyboardButton>> inline_keyboard = new ArrayList<>();

        for (int i = 0; i < queue.length(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append(". ");
            sb.append(queue.getString(i).equals("_") ? ButtonsText.EMPTY_QUEUE_BUTTON_LARGE : MongoDB.getUserName(queue.getString(i).trim()));
            button.setText(sb.toString());
            sb = new StringBuilder();

            sb.append(queue.getString(i).equals("_") ? CallbackData.QueueIn.toString() : CallbackData.QueueOut.toString());
            sb.append(i);
            button.setCallbackData(sb.toString());

            inline_keyboard.add(List.of(button));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(inline_keyboard);
        return markup;
    }

    private static InlineKeyboardMarkup largeQueueConstructor(JSONArray queue) {
        int countOfRows = 10, maxColumnsCount = 2, length = queue.length();

        if(length % countOfRows == 0) {
            countOfRows = length/2;
        } else {
            do {
                countOfRows ++;
            }
            while (length / countOfRows >= maxColumnsCount);
            if(length % 2 == 0) countOfRows--;
        }

        List<List<InlineKeyboardButton>> inline_keyboard = new ArrayList<>();

        for (int i = 0; i < countOfRows; i++) {
            ArrayList<InlineKeyboardButton> row = new ArrayList<>();

            for (int j = 0; j < maxColumnsCount; j++) {
                int index = i + countOfRows * j;
                InlineKeyboardButton button2 = new InlineKeyboardButton();
                if(index < length) {
                    button2.setText(
                            new ResponseTextBuilder()
                                    .addText(index)
                                    .addText(".")
                                    .addText(queue.getString(index).equals("_") ?
                                            ButtonsText.EMPTY_QUEUE_BUTTON_SMALL
                                            : MongoDB.getUserName(queue.getString(index).trim()))
                                    .get()
                    );
                    StringBuilder sb = new StringBuilder();
                    sb.append(queue.getString(index).equals("_") ? CallbackData.QueueIn.toString() : CallbackData.QueueOut.toString());
                    sb.append(index);
                    button2.setCallbackData(sb.toString());
                } else {
                    button2.setText(ButtonsText.EMPTY_QUEUE_BUTTON_SMALL);
                    button2.setCallbackData(" ");
                }
                row.add(button2);
            }
            inline_keyboard.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(inline_keyboard);
        return markup;
    }
}
