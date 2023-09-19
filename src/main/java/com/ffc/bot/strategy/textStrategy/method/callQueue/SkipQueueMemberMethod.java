package main.java.com.ffc.bot.strategy.textStrategy.method.callQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class SkipQueueMemberMethod implements TextStrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
        if(queueState.equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
            JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

            String userId = QueueCallModule.getFirstQueueUser(queue);

            queue.remove(queue.toList().indexOf(userId));
            queue.put(MongoDB.EMPTY_QUEUE_MEMBER);
            MongoDB.updateQueue(queue.toString(),chatId);

            List<BotApiMethod> methods = new ArrayList<>(new NotifyFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId));
            return methods;
        } else if(queueState.equalsIgnoreCase(QueueState.IN_PROCESS.toString())) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.ERROR_CALL_QUEUE_IS_NOT_STARTED, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.START_CALL_QUEUE, TextFormat.Bold)
                            .get()
                    );
            return List.of(response);
        }
        return null;
    }
}
