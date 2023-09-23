package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.NotifyFirstAndNextQueueUsersMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class CalledOutMethod implements CallbackStrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        String[] chatAndUserIds = callbackUpdate.split(CallbackData.CalledOut.toString());
        userId = chatAndUserIds[1].trim();
        chatId = chatAndUserIds[0];

        response.setChatId(chatId);

        if(MongoDB.queueExists(chatId)) {

            PersonalSendMessage personalMessage = new PersonalSendMessage();
            personalMessage.setChatId(userId);

            if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {

                JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

                if(QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(userId)) {
                    List<BotApiMethod> methods;

                    queue.remove(queue.toList().indexOf(userId));
                    queue.put(MongoDB.EMPTY_QUEUE_MEMBER);
                    MongoDB.updateQueue(queue.toString(),chatId);

                    methods = new ArrayList<>(new NotifyFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId));

                    personalMessage.setText(
                            new ResponseTextBuilder()
                                    .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                    .addTextLine()
                                    .addTextLine(CallQueueResponse.CALL_QUEUE_LEFT)
                                    .get()
                    );
                    methods.add(personalMessage);
                    return methods;
                } else {
                    if(queue.toList().contains(userId)) {
                        personalMessage.setText(
                                new ResponseTextBuilder()
                                        .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                        .addTextLine()
                                        .addTextLine(CallQueueResponse.ERROR_CAUSE_USER_IS_NO_LONGER_FIRST, TextFormat.Monocular)
                                        .get()
                        );
                    } else {
                        personalMessage.setText(
                                new ResponseTextBuilder()
                                        .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                        .addTextLine()
                                        .addTextLine(CallQueueResponse.ERROR_CAUSE_USER_IS_NO_LONGER_IN_QUEUE, TextFormat.Monocular)
                                        .get()
                        );
                    }
                    return List.of(personalMessage);
                }
            } else {
                personalMessage.setText(
                        new ResponseTextBuilder()
                                .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                .addTextLine()
                                .addTextLine(CallQueueResponse.ERROR_CALL_QUEUE_IS_NOT_STARTED, TextFormat.Monocular)
                                .get()
                );
                return List.of(personalMessage);
            }
        } else {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(DefaultQueueResponse.QUEUE_DOES_NOT_EXISTS, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.ERROR_CALL_QUEUE_IS_NOT_STARTED, TextFormat.Monocular)
                            .get()
            );
            return List.of(response);
        }
    }
}
