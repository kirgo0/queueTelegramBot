package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class SaveQueueMethod implements TextStrategyMethod {

    private final String textUpdate;

    public SaveQueueMethod(String textUpdate) {
        this.textUpdate = textUpdate.replace("/savequeue", "").trim();
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        String savedQueueName = textUpdate;

        if(savedQueueName.length() == 0) {
            return null;
        }

        if(savedQueueName.equalsIgnoreCase(MongoDB.DEFAULT_SAVED_QUEUE_NAME)) {
            response.setText(new ResponseTextBuilder()
                    .addText(WorkWithQueueResponse.ERROR_CAUSE_SAVED_QUEUE_WRONG_NAME, TextFormat.Monocular)
                    .addTextLine()
                    .startFormat(TextFormat.Bold)
                    .addTextLine(BotCommandsResponse.SAVE_QUEUE)
                    .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES)
                    .endFormat(TextFormat.Bold)
                    .get()
            );
        }
        // if user trying to save queue with name that is already taken
        else if(MongoDB.savedQueueExists(chatId, savedQueueName)) {

            response.setText(new ResponseTextBuilder()
                    .addText(WorkWithQueueResponse.SAVED_QUEUE_ALREADY_EXISTS, TextFormat.Monocular)
                    .addTextLine()
                    .startFormat(TextFormat.Bold)
                    .addTextLine(BotCommandsResponse.RESAVE_QUEUE)
                    .addTextLine(BotCommandsResponse.SAVE_QUEUE)
                    .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES)
                    .endFormat(TextFormat.Bold)
                    .get()
            );


        }
        // if user trying to create more than 5 different saves
        else if(MongoDB.getSavedQueuesCount(chatId) > 5) {
            response.setText(new ResponseTextBuilder()
                    .addText(WorkWithQueueResponse.MAX_SAVED_QUEUES_COUNT_REACHED)
                    .addTextLine()
                    .startFormat(TextFormat.Bold)
                    .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES)
                    .endFormat(TextFormat.Bold)
                    .get()
            );

        } else {
            String savedQueue = MongoDB.getQueue(chatId);

            MongoDB.createNewSavedQueue(chatId, savedQueueName, savedQueue);

            response.setText(new ResponseTextBuilder()
                    .startFormat(TextFormat.Italic)
                    .addText(WorkWithQueueResponse.QUEUE_SAVED_AS).addText(savedQueueName)
                    .endFormat(TextFormat.Italic)
                    .addTextLine()
                    .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES,TextFormat.Bold)
                    .get()
            );
        }

        return List.of(response);
    }
}
