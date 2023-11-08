package main.java.com.ffc.bot.strategy.callbackStrategy.method.sheduledTasks;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.scheduler.WeekNumber;
import main.java.com.ffc.bot.strategy.SchedulerCallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class ChangeWeekNumberForTaskMethod implements CallbackStrategyMethod {

    String clickedMessageId;
    TaskScheduler scheduler;

    public ChangeWeekNumberForTaskMethod(String clickedMessageId, TaskScheduler scheduler) {
        this.clickedMessageId = clickedMessageId;
        this.scheduler = scheduler;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        boolean isFirst = callbackUpdate.contains(SchedulerCallbackData.SchedulerFirstWeek.toString());

        String taskName;
        if(isFirst) {
            taskName = callbackUpdate.replaceFirst(SchedulerCallbackData.SchedulerFirstWeek.toString(),"");
        } else {
            taskName = callbackUpdate.replaceFirst(SchedulerCallbackData.SchedulerSecondWeek.toString(),"");
        }

        var task = MongoDB.getScheduledTask(chatId,taskName);
        if(task == null) return null;

        if(isFirst) {
            task.setWeekNumber(WeekNumber.FIRST);
        } else {
            task.setWeekNumber(WeekNumber.SECOND);
        }

        if(MongoDB.updateScheduledTask(chatId, taskName, task)) {
            return new UpdateTimeForTaskMethod(scheduler).getResponse(clickedMessageId,taskName,chatId,task);
        } else {
            return null;
        }
    }
}
