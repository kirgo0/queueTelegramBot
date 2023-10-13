package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class RemoveSavedQueueMethod implements CallbackStrategyMethod {

    String clickedMessageId;

    public RemoveSavedQueueMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {
        
        String savedQueueName = callbackUpdate.replaceFirst(CallbackData.RemoveSavedQueue.toString(),"");
        
        response.setText(
                new ResponseTextBuilder()
                        .addText(WorkWithQueueResponse.REMOVE_SAVED_QUEUE_AUTH)
                        .addText("\"").addText(savedQueueName).addText("\"")
                        .get()
                );

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton acceptButton = new InlineKeyboardButton();
        acceptButton.setText("✅");
        StringBuilder sb = new StringBuilder();
        sb.append(CallbackData.AcceptSavedQueue).append(savedQueueName);
        acceptButton.setCallbackData(sb.toString());

        InlineKeyboardButton denyButton = new InlineKeyboardButton();
        denyButton.setText("❌ ");
        sb = new StringBuilder();
        sb.append(CallbackData.DenySavedQueue).append(savedQueueName);
        denyButton.setCallbackData(sb.toString());

        keyboard.add(List.of(acceptButton,denyButton));
        response.setReplyMarkup(new InlineKeyboardMarkup(keyboard));

        return List.of(response);
    }
}
