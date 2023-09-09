package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.queueHandler.QueueResizeModule;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class SetDefaultQueueSizeMethod implements StrategyMethod {

    private String textUpdate;

    public SetDefaultQueueSizeMethod(String textUpdate) {
        this.textUpdate = textUpdate;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        textUpdate = textUpdate.replace("/setdqs","");

        int nextQueueSize = Integer.parseInt(textUpdate.trim());

        if(nextQueueSize <= 1 || nextQueueSize > 100) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.CANT_SET_QUEUE_SIZE, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                            .get()
                    );
            response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
            return List.of(response);
        }

        if(MongoDB.getFieldValue(MongoDB.DEFAULT_QUEUE_SIZE,chatId).equalsIgnoreCase(String.valueOf(nextQueueSize))) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.CURRENT_QUEUE_SIZE, TextFormat.Italic)
                            .addText(String.valueOf(nextQueueSize),TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                            .get()
            );
            response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
            return List.of(response);
        }

        String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);

        // if queue is not closed
        if(!queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
            JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

            boolean queueResized = QueueResizeModule.changeSize(queue, nextQueueSize);

            ResponseTextBuilder responseTextBuilder = new ResponseTextBuilder();

            responseTextBuilder
                    .addText(WorkWithQueueResponse.DEFAULT_QUEUE_SIZE_CHANGED, TextFormat.Italic)
                    .addText(String.valueOf(nextQueueSize), TextFormat.Italic);

            MongoDB.updateField(MongoDB.DEFAULT_QUEUE_SIZE, String.valueOf(nextQueueSize),chatId);

            // if current queue can be resized
            if(queueResized) {
                MongoDB.updateQueue(queue.toString(),chatId);
                responseTextBuilder
                        .addTextLine(WorkWithQueueResponse.CURRENT_QUEUE_SIZE_CHANGED, TextFormat.Italic)
                        .addTextLine()
                        .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE);
            } else {
                responseTextBuilder.addTextLine(WorkWithQueueResponse.CANT_CHANGE_QUEUE_SIZE, TextFormat.Monocular);
            }
            response.setText(responseTextBuilder.get());
            response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
            return List.of(response);
        }

        // if queue is closed
        if(queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
            MongoDB.updateField(MongoDB.DEFAULT_QUEUE_SIZE, String.valueOf(nextQueueSize),chatId);

            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.DEFAULT_QUEUE_SIZE_CHANGED, TextFormat.Italic)
                            .addText(String.valueOf(nextQueueSize), TextFormat.Italic)
                            .get()
            );
            return List.of(response);
        }
        return null;
    }
}
