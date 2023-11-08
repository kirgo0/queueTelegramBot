package main.java.com.ffc.bot.strategy.callbackStrategy;

import main.java.com.ffc.bot.*;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.SchedulerCallbackData;
import main.java.com.ffc.bot.strategy.Strategy;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.*;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.sheduledTasks.*;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.swap.SwapAcceptedMethod;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.swap.SwapDeniedMethod;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.workWithQueue.*;
import main.java.com.ffc.bot.strategy.textStrategy.method.CheckAuthoriseMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.scheduledTasks.GetScheduledTasksMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CallbackUpdateStrategy implements Strategy {

    TaskScheduler scheduler;

    public CallbackUpdateStrategy(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

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
        response.setParseMode("HTML");

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
        else if (callbackUpdate.contains(CallbackData.GetSavedQueue.toString())) {
            responseMethod = new GetSavedQueueMethod(clickedMessageId);
        }
        else if (callbackUpdate.contains(CallbackData.SaveQueueBackMenu.toString())) {
            responseMethod = new SavedQueueBackMenuMethod(clickedMessageId);
        }
        else if (callbackUpdate.contains(CallbackData.RemoveSavedQueue.toString())) {
            responseMethod = new RemoveSavedQueueMethod();
        }
        else if (callbackUpdate.contains(CallbackData.LoadSavedQueue.toString())) {
            responseMethod = new LoadSavedQueueMethod(clickedMessageId);
        }
        else if (callbackUpdate.contains(CallbackData.AcceptSavedQueue.toString())) {
            responseMethod = new AcceptRemoveSavedQueueMethod();
        }
        else if (callbackUpdate.contains(CallbackData.DenySavedQueue.toString())) {
            responseMethod = new DenyRemoveSavedQueueMethod(clickedMessageId);
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerGet.toString())) {
            responseMethod = new GetTaskMethod(clickedMessageId);
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerMenu.toString())) {
            responseMethod = new GetScheduledTasksMethod(clickedMessageId);
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerTaskDelete.toString())) {
            responseMethod = new DeleteScheduledTaskMethod();
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerAcceptTaskDelete.toString())) {
            responseMethod = new AcceptDeleteScheduledTaskMethod(scheduler);
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerPlusMin.toString()) ||
                callbackUpdate.contains(SchedulerCallbackData.SchedulerMinusMin.toString())) {
            responseMethod = new ChangeMinutesForTaskMethod(clickedMessageId, scheduler);
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerPlusDay.toString()) ||
                callbackUpdate.contains(SchedulerCallbackData.SchedulerMinusDay.toString())) {
            responseMethod = new ChangeDayOfWeekForTaskMethod(clickedMessageId, scheduler);
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerFirstWeek.toString()) ||
                callbackUpdate.contains(SchedulerCallbackData.SchedulerSecondWeek.toString())) {
            responseMethod = new ChangeWeekNumberForTaskMethod(clickedMessageId, scheduler);
        }
        else if (callbackUpdate.contains(SchedulerCallbackData.SchedulerPlusQueueSize.toString()) ||
                callbackUpdate.contains(SchedulerCallbackData.SchedulerMinusQueueSize.toString())) {
            responseMethod = new ChangeQueueSizeForTaskMethod(clickedMessageId);
        }

        if(responseMethod != null) return responseMethod.getResponse(update, response, chatId, userId, callbackUpdate);
        return null;
    }
}
