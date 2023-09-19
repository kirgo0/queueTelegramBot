package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.HttpClient;
import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CreateQueueMethod implements TextStrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        if(!MongoDB.chatRegistered(chatId)) {
            MongoDB.createNewQueue(chatId);
        } else if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.IN_PROCESS.toString())) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.ERROR_CAUSE_QUEUE_IS_OPENED, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.CLOSE_QUEUE, TextFormat.Bold)
                            .get()
            );
            response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
            return List.of(response);
        } else if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.CLOSED.toString())) {
            MongoDB.updateQueue("",chatId);
            MongoDB.updateField(MongoDB.QUEUE_STATE,QueueState.IN_PROCESS.toString(),chatId);
            MongoDB.clearSwapRequests(chatId);
        }

        // default queue length (works only if chat members count can't be determined)
        int chatMembersCount = 10;

        // get chat members count
        try {
            String defaultQueueSize = MongoDB.getFieldValue(MongoDB.DEFAULT_QUEUE_SIZE,chatId);
            if(defaultQueueSize.equalsIgnoreCase("0")) {
                chatMembersCount = HttpClient.getChannelMembersCount(chatId);
            } else {
                chatMembersCount = Integer.parseInt(defaultQueueSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // new array for DB
        JSONArray array = new JSONArray();

        // insert empty values
        for (int i = 0; i < chatMembersCount; i++) {
            array.put(MongoDB.EMPTY_QUEUE_MEMBER);
        }
        MongoDB.updateQueue(array.toString(),chatId);

        response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
        return List.of(response);
    }
}
