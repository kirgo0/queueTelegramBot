package main.java.com.ffc.bot.strategy.callbackStrategy.method.sheduledTasks;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.strategy.SchedulerCallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class ChangeDayOfWeekForTaskMethod implements CallbackStrategyMethod {

    String clickedMessageId;
    TaskScheduler scheduler;

    public ChangeDayOfWeekForTaskMethod(String clickedMessageId, TaskScheduler scheduler) {
        this.clickedMessageId = clickedMessageId;
        this.scheduler = scheduler;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        boolean isPositive = callbackUpdate.contains(SchedulerCallbackData.SchedulerPlusDay.toString());

        String[] params;
        if(isPositive) {
            params = callbackUpdate.split(SchedulerCallbackData.SchedulerPlusDay.toString());
        } else {
            params = callbackUpdate.split(SchedulerCallbackData.SchedulerMinusDay.toString());
        }

        if(params.length != 2) return null;

        var taskName = params[1];

        var task = MongoDB.getScheduledTask(chatId,taskName);
        if(task == null) return null;

        if(isPositive) {
            task.setDayOfWeek(task.getDayOfWeek().plus(1));
        } else {
            task.setDayOfWeek(task.getDayOfWeek().minus(1));
        }

        if(MongoDB.updateScheduledTask(chatId, taskName, task)) {
            return new UpdateTimeForTaskMethod(scheduler).getResponse(clickedMessageId,taskName,chatId,task);
        } else {
            return null;
        }
    }
}
