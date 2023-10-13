package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.markupConstructor.SavedQueuesConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class AcceptRemoveSavedQueueMethod implements CallbackStrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        String savedQueueName = callbackUpdate.replaceFirst(CallbackData.AcceptSavedQueue.toString(),"");

        MongoDB.removeSavedQueue(chatId,savedQueueName);

        response.setText(new ResponseTextBuilder()
                .addText(WorkWithQueueResponse.SAVED_QUEUE_REMOVED, TextFormat.Italic)
                .addTextLine()
                .addTextLine(WorkWithQueueResponse.SAVED_QUEUES_LIST, TextFormat.Italic)
                .get()
        );

        response.setReplyMarkup(SavedQueuesConstructor.getSavedQueuesMarkup(chatId));

        return List.of(response);
    }
}
