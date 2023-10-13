package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class ReSaveQueueMethod implements TextStrategyMethod {

    private final String textUpdate;

    public ReSaveQueueMethod(String textUpdate) {
        this.textUpdate = textUpdate.replace("/resavequeue", "").trim();
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        String savedQueueName = textUpdate;

        if(savedQueueName.length() == 0) {
            return null;
        }

        if(MongoDB.savedQueueExists(chatId, savedQueueName)) {
            MongoDB.updateSavedQueue(chatId, savedQueueName);

            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.RESAVED_QUEUE, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES, TextFormat.Bold)
                            .get()
            );
        } else {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.ERROR_CAUSE_SAVED_QUEUE_DOES_NOT_EXISTS, TextFormat.Monocular)
                            .addTextLine()
                            .startFormat(TextFormat.Bold)
                            .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES)
                            .addTextLine(BotCommandsResponse.SAVE_QUEUE)
                            .endFormat(TextFormat.Bold)
                            .get()
            );
        }

        return List.of(response);
    }
}
