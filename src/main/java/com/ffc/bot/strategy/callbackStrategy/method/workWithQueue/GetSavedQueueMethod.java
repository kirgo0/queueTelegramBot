package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.markupConstructor.SavedQueuesConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class GetSavedQueueMethod implements CallbackStrategyMethod {

    String clickedMessageId;

    public GetSavedQueueMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        EditMessageText newMessage = new EditMessageText();
        newMessage.setParseMode("HTML");

        newMessage.setChatId(chatId);
        newMessage.setMessageId(Integer.parseInt(clickedMessageId));

        String savedQueueName = callbackUpdate.replaceFirst(CallbackData.GetSavedQueue.toString(),"");

        newMessage.setText(
                new ResponseTextBuilder()
                    .startFormat(TextFormat.Italic)
                    .addText(WorkWithQueueResponse.GET_SAVED_QUEUE)
                    .addText("\"")
                    .addText(savedQueueName)
                    .addText("\"")
                    .endFormat(TextFormat.Italic)
                    .get()
        );

        newMessage.setReplyMarkup(SavedQueuesConstructor.getSavedQueueMarkup(chatId, savedQueueName));

        return List.of(newMessage);
    }
}
