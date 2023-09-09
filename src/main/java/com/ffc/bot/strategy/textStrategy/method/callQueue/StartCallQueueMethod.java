package main.java.com.ffc.bot.strategy.textStrategy.method.callQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class StartCallQueueMethod implements StrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        if(MongoDB.queueExists(chatId)) {

            String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);

            if(queueState.equalsIgnoreCase(QueueState.IN_PROCESS.toString())) {

                JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));
                if(QueueCallModule.getCountOfQueueUsers(queue) < 2) {

                    response.setText(
                            new ResponseTextBuilder()
                                    .addText(CallQueueResponse.QUEUE_IS_NOT_FULL, TextFormat.Monocular)
                                    .addTextLine()
                                    .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                                    .get()
                            );
                    response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));

                    return List.of(response);
                }

                MongoDB.updateField(MongoDB.QUEUE_STATE,QueueState.QUEUE_STARTED.toString(),chatId);

                StrategyMethod method = new GetFirstAndNextQueueUsersMethod(queue);
                return method.getResponse(update,response,chatId);
            } else if(queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(DefaultQueueResponse.QUEUE_IS_NOT_OPENED, TextFormat.Monocular)
                                .addTextLine()
                                .addTextLine(BotCommandsResponse.OPEN_QUEUE, TextFormat.Bold)
                                .get()
                        );
                return List.of(response);
            } else {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                .addTextLine()
                                .addTextLine(CallQueueResponse.DEFAULT_RESPONSE)
                                .get()
                        );
                response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
                return List.of(response);
            }
        } else {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(DefaultQueueResponse.QUEUE_DOES_NOT_EXISTS)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.CREATE_QUEUE, TextFormat.Bold)
                            .get()
            );
            return List.of(response);
        }
    }
}
