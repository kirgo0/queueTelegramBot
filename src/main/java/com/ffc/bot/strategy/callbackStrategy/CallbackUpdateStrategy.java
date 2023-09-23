package main.java.com.ffc.bot.strategy.callbackStrategy;

import main.java.com.ffc.bot.*;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.Strategy;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.*;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.swap.SwapAcceptedMethod;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.swap.SwapDeniedMethod;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue.CalledOutMethod;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue.QueueInMethod;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue.QueueOutMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.CheckAuthoriseMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CallbackUpdateStrategy implements Strategy {
    @Override
    public List<BotApiMethod> getResponse(Update update) {

        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String userId = update.getCallbackQuery().getFrom().getId().toString();
        String callbackUpdate = update.getCallbackQuery().getData();

        if(!MongoDB.userExists(userId)) {
            MongoDB.createNewUser(userId, update.getCallbackQuery().getMessage().getFrom().getFirstName());
        } else {
            MongoDB.updateUser(userId, update.getCallbackQuery().getFrom().getFirstName());
        }

        CallbackStrategyMethod responseMethod = null;

        if(!MongoDB.userAuthorised(userId)) {
            if(!update.getCallbackQuery().getMessage().isReply()) {
                return new CheckAuthoriseMethod().getResponse(update,null, chatId);
            } else {
                return null;
            }
        }

        SendMessage response = new SendMessage();
        response.setChatId(chatId);

        String clickedMessageId = update.getCallbackQuery().getMessage().getMessageId().toString();

        // check if user clicked on empty button
        if(callbackUpdate.contains(CallbackData.QueueIn.toString())) {
            responseMethod = new QueueInMethod(clickedMessageId);
        }
        else if(callbackUpdate.contains(CallbackData.QueueOut.toString())) {
            responseMethod = new QueueOutMethod(clickedMessageId);
        }
        else if(callbackUpdate.contains(CallbackData.SwapDenied.toString())) {
            responseMethod = new SwapDeniedMethod();
        }
        else if(callbackUpdate.contains(CallbackData.SwapAccepted.toString())) {
            responseMethod = new SwapAcceptedMethod();
        }
        else if(callbackUpdate.contains(CallbackData.CalledOut.toString())) {
            responseMethod = new CalledOutMethod();
        }
        else if(callbackUpdate.contains(CallbackData.GetInfo.toString())) {
            responseMethod = new CallbackGetInfoMethod();
        }

        if(responseMethod != null) return responseMethod.getResponse(update, response, chatId, userId, callbackUpdate);
        return null;
    }
}
