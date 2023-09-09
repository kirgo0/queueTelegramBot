package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class GetQueueMethod implements StrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        if(!MongoDB.queueExists(chatId)) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(DefaultQueueResponse.QUEUE_DOES_NOT_EXISTS)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.CREATE_QUEUE, TextFormat.Bold)
                            .get()
            );
            return List.of(response);
        }
        String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
        if(queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(DefaultQueueResponse.QUEUE_IS_NOT_OPENED, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.OPEN_QUEUE, TextFormat.Bold)
                            .addTextLine(BotCommandsResponse.CREATE_QUEUE, TextFormat.Bold)
                            .get()
            );
            return List.of(response);
        } else if(queueState.equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
            response.setText(CallQueueResponse.DEFAULT_RESPONSE);
        }
        response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
        return List.of(response);
    }
}
