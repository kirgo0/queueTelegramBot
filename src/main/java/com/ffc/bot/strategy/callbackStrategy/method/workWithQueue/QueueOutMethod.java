package main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.markupConstructor.SwapRequestConstructor;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SwapResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.NotifyFirstAndNextQueueUsersMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class QueueOutMethod implements CallbackStrategyMethod {


    private String clickedMessageId;

    public QueueOutMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {
        int queuePos = Integer.parseInt(callbackUpdate.replace(CallbackData.QueueOut.toString(),""));

        try {
            JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));
            if(queue.get(queuePos).equals(userId)) {

                queue.put(queuePos,MongoDB.EMPTY_QUEUE_MEMBER);

                MongoDB.updateQueue(queue.toString(),chatId);

                // Executing api method EditMessageReplyMarkup
                EditMessageReplyMarkup newResponse = new EditMessageReplyMarkup();

                newResponse.setChatId(chatId);
                newResponse.setMessageId(Integer.parseInt(clickedMessageId));

                // change message reply markup
                newResponse.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));

                if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
                    return new NotifyFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId);
                }
                return List.of(newResponse);
            }

            // check if user clicked on other user, this means they want to swap
            if(queue.toList().contains(userId)) {
                // get both user ids
                String firstUserId = (String) queue.get(queue.toList().indexOf(userId));
                String secondUserId = (String) queue.get(queuePos);

                // check if swap request between both users already exists
                if(!MongoDB.swapRequestExists(chatId,firstUserId,secondUserId) && !MongoDB.swapRequestExists(chatId,secondUserId,firstUserId)) {
                    MongoDB.createNewSwapRequest(chatId,firstUserId,secondUserId,queue.toList().indexOf(userId),queuePos);
                }

                return SwapRequestConstructor.getSwapRequest(chatId, firstUserId, secondUserId, userId, queue, queuePos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
