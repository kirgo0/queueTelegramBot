package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.markupConstructor.SavedQueuesConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class SavedQueueBackMenuMethod implements CallbackStrategyMethod {

    String clickedMessageId;

    public SavedQueueBackMenuMethod(String clickedMessage) {
        this.clickedMessageId = clickedMessage;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        EditMessageText newMessage = new EditMessageText();
        newMessage.setParseMode("HTML");

        newMessage.setChatId(chatId);
        newMessage.setMessageId(Integer.parseInt(clickedMessageId));

        newMessage.setText(
                new ResponseTextBuilder()
                    .addText(WorkWithQueueResponse.SAVED_QUEUES_LIST, TextFormat.Italic)
                    .get()
        );

        newMessage.setReplyMarkup(SavedQueuesConstructor.getSavedQueuesMarkup(chatId));

        return List.of(newMessage);
    }
}
