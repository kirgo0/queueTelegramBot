package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.NotifyFirstAndNextQueueUsersMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class QueueInMethod implements CallbackStrategyMethod {

    private String clickedMessageId;

    public QueueInMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        List<BotApiMethod> methods = new ArrayList<>();
        // get button position
        int queuePos = Integer.parseInt(callbackUpdate.replace(CallbackData.QueueIn.toString(),""));

        try {
            JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));
            boolean userInQueue = false;
            for (var user :
                    queue) {
                if (user.equals(userId)) {
                    userInQueue = true;
                    break;
                }
            }

            boolean userWasFirst = false;
            boolean userWasNext = false;

            String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
            if(userInQueue) {
                if(QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(userId)) {
                    userWasFirst = true;
                }
                if(QueueCallModule.getNextQueueUser(queue).equalsIgnoreCase(userId)) {
                    userWasNext = true;
                }
                queue.put(queue.toList().indexOf(userId),MongoDB.EMPTY_QUEUE_MEMBER);
            }
            queue.put(queuePos,userId);

            MongoDB.updateQueue(queue.toString(),chatId);

            // Executing api method EditMessageReplyMarkup
            EditMessageReplyMarkup newResponse = new EditMessageReplyMarkup();

            newResponse.setChatId(chatId);
            newResponse.setMessageId(Integer.parseInt(clickedMessageId));

            // change message reply markup
            newResponse.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
            if(queueState.equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
                boolean userNowFirst = QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(userId);
                boolean userNowNext = QueueCallModule.getNextQueueUser(queue).equalsIgnoreCase(userId);
                if ((!userWasFirst || !userNowFirst) && (!userWasNext || !userNowNext)) {
                    methods = new ArrayList<>(new NotifyFirstAndNextQueueUsersMethod(queue, true).getResponse(update,response,chatId));
                    return methods;
                }
            }
            methods.add(newResponse);
            return methods;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
