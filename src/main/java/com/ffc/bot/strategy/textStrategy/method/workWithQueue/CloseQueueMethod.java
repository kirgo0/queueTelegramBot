package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
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

public class CloseQueueMethod implements StrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
        if(queueState.equalsIgnoreCase(QueueState.IN_PROCESS.toString())) {
            MongoDB.updateField(MongoDB.QUEUE_STATE,QueueState.CLOSED.toString(),chatId);
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.QUEUE_IS_CLOSED, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.CREATE_QUEUE, TextFormat.Bold)
                            .addTextLine(BotCommandsResponse.OPEN_QUEUE, TextFormat.Bold)
                            .get()
                    );
            return List.of(response);
        } else if (queueState.equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.CALL_QUEUE_IS_RUNNING, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.STOP_CALL_QUEUE, TextFormat.Bold)
                            .get()
            );
            return List.of(response);
        } else {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(DefaultQueueResponse.QUEUE_IS_NOT_OPENED, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.OPEN_QUEUE, TextFormat.Bold)
                            .get()
            );
            return List.of(response);
        }

    }
}
