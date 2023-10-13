package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class LoadSavedQueueMethod implements CallbackStrategyMethod {

    String clickedMessageId;

    public LoadSavedQueueMethod(String clickedMessage) {
        this.clickedMessageId = clickedMessage;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        String savedQueueName = callbackUpdate.replaceFirst(CallbackData.LoadSavedQueue.toString(),"");
        String savedQueue = MongoDB.getSavedQueue(chatId, savedQueueName);

        if(MongoDB.savedQueueExists(chatId, MongoDB.DEFAULT_SAVED_QUEUE_NAME)) {
            MongoDB.updateDefaultSavedQueue(chatId);
        } else {
            MongoDB.createNewDefaultSavedQueue(chatId);
        }

        MongoDB.updateField(MongoDB.DEFAULT_QUEUE_SIZE, String.valueOf(new JSONArray(savedQueue).length()), chatId);

        MongoDB.updateQueue(savedQueue, chatId);

        response.setText(
                new ResponseTextBuilder()
                        .startFormat(TextFormat.Italic)
                        .addText(WorkWithQueueResponse.SAVED_QUEUE_LOADED)
                        .addText("\"").addText(savedQueueName).addText("\"")
                        .endFormat(TextFormat.Italic)
                        .addTextLine()
                        .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES, TextFormat.Bold)
                        .addTextLine()
                        .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                        .get()
        );
        response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));

        return List.of(response);
    }
}
