package main.java.com.ffc.bot.markupConstructor;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.strategy.CallbackData;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class SavedQueuesConstructor {

    public static InlineKeyboardMarkup getSavedQueuesMarkup(String chatId) {

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        ArrayList<String> names = MongoDB.getSavedQueuesNames(chatId);

        for (String name : names) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(name);
            StringBuilder sb = new StringBuilder();
            sb.append(CallbackData.GetSavedQueue).append(name);
            button.setCallbackData(sb.toString());
            row.add(button);

            if(name.equalsIgnoreCase(MongoDB.DEFAULT_SAVED_QUEUE_NAME))
                keyboard.add(0, row);
            else
                keyboard.add(row);
        }

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getSavedQueueMarkup(String chatId, String savedQueueName) {

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        JSONArray queue = new JSONArray(MongoDB.getSavedQueue(chatId,savedQueueName));

        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("↪️");
        backButton.setCallbackData(CallbackData.SaveQueueBackMenu.toString());
        row.add(backButton);

        InlineKeyboardButton loadButton = new InlineKeyboardButton();
        loadButton.setText("✅");
        StringBuilder sb = new StringBuilder();
        sb.append(CallbackData.LoadSavedQueue).append(savedQueueName);
        loadButton.setCallbackData(sb.toString());
        row.add(loadButton);

        if(!savedQueueName.equalsIgnoreCase(MongoDB.DEFAULT_SAVED_QUEUE_NAME)) {
            InlineKeyboardButton removeButton = new InlineKeyboardButton();
            removeButton.setText("❌ ");
            sb = new StringBuilder();
            sb.append(CallbackData.RemoveSavedQueue).append(savedQueueName);
            removeButton.setCallbackData(sb.toString());
            row.add(removeButton);
        }

        keyboard.add(row);

        for (int i = 0; i < queue.length(); i++) {
            String userName;
            try {
                userName = MongoDB.getUserName(queue.getString(i));
            } catch (Exception e) {
                userName = MongoDB.EMPTY_QUEUE_MEMBER;
            }
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(userName);
            button.setCallbackData(" ");
            keyboard.add(List.of(button));
        }

        return new InlineKeyboardMarkup(keyboard);
    }
}
