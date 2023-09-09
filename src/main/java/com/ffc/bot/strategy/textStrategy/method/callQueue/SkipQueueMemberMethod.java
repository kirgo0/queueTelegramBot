package main.java.com.ffc.bot.strategy.textStrategy.method.callQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class SkipQueueMemberMethod implements StrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {

        if(MongoDB.queueExists(chatId)) {
            String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
            if(queueState.equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
                JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

                String userId = QueueCallModule.getFirstQueueUser(queue);

                queue.remove(queue.toList().indexOf(userId));
                queue.put(MongoDB.EMPTY_QUEUE_MEMBER);
                MongoDB.updateQueue(queue.toString(),chatId);

                List<BotApiMethod> methods = new ArrayList<>(new GetFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId));
                return methods;
            } else if (queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
                response.setText(
                        new ResponseTextBuilder()
                        .addText(DefaultQueueResponse.QUEUE_IS_NOT_OPENED, TextFormat.Monocular)
                        .addTextLine(BotCommandsResponse.OPEN_QUEUE, TextFormat.Bold)
                        .get()
                );
                return List.of(response);
            } else {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(CallQueueResponse.ERROR_CALL_QUEUE_IS_NOT_STARTED)
                                .addTextLine(BotCommandsResponse.STOP_CALL_QUEUE, TextFormat.Bold)
                                .get()
                        );
                return List.of(response);
            }
        } else {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(DefaultQueueResponse.QUEUE_DOES_NOT_EXISTS)
                            .addTextLine(BotCommandsResponse.CREATE_QUEUE, TextFormat.Bold)
                            .get()
                    );
            return List.of(response);
        }
    }
}
