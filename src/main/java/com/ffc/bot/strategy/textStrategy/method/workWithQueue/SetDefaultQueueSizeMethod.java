package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.queueHandler.QueueResizeModule;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class SetDefaultQueueSizeMethod implements TextStrategyMethod {

    private String textUpdate;

    public SetDefaultQueueSizeMethod(String textUpdate) {
        this.textUpdate = textUpdate;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        textUpdate = textUpdate.replace("/setdqs", "");

        int nextQueueSize = Integer.parseInt(textUpdate.trim());

        if (nextQueueSize <= 1 || nextQueueSize > 100) {
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

        String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE, chatId);
        if (MongoDB.getFieldValue(MongoDB.DEFAULT_QUEUE_SIZE, chatId).equalsIgnoreCase(String.valueOf(nextQueueSize))) {
            ResponseTextBuilder responseTextBuilder = new ResponseTextBuilder();
            responseTextBuilder.addText(WorkWithQueueResponse.CURRENT_QUEUE_SIZE, TextFormat.Italic)
                    .addText(String.valueOf(nextQueueSize), TextFormat.Italic)
                    .addTextLine();
            if(MongoDB.queueExists(chatId) && !queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
                responseTextBuilder.addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE);
                response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
            }
            response.setText(responseTextBuilder.get());
            return List.of(response);
        }

        MongoDB.updateField(MongoDB.DEFAULT_QUEUE_SIZE, String.valueOf(nextQueueSize), chatId);

        ResponseTextBuilder responseTextBuilder = new ResponseTextBuilder();
        responseTextBuilder
                .addText(WorkWithQueueResponse.DEFAULT_QUEUE_SIZE_CHANGED, TextFormat.Italic)
                .addText(String.valueOf(nextQueueSize), TextFormat.Italic)
                .addTextLine();

        if (MongoDB.queueExists(chatId)) {
                JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

                boolean queueResized = QueueResizeModule.changeSize(queue, nextQueueSize);

                MongoDB.updateField(MongoDB.DEFAULT_QUEUE_SIZE, String.valueOf(nextQueueSize), chatId);

                // if current queue can be resized
                if (queueResized) {
                    MongoDB.updateQueue(queue.toString(), chatId);
                    if(!queueState.equalsIgnoreCase(QueueState.CLOSED.toString()))
                        responseTextBuilder
                            .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE);
                } else {
                    responseTextBuilder.addTextLine(WorkWithQueueResponse.CANT_CHANGE_QUEUE_SIZE, TextFormat.Monocular);
                }

                response.setText(responseTextBuilder.get());
                if(!queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
                return List.of(response);
//                }
        }
        else {
            response.setText(responseTextBuilder.get());
            return List.of(response);
        }
    }
}
