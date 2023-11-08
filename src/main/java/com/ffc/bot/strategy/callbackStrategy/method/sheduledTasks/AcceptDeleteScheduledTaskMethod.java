package main.java.com.ffc.bot.strategy.callbackStrategy.method.sheduledTasks;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.strategy.SchedulerCallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.scheduledTasks.GetScheduledTasksMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class AcceptDeleteScheduledTaskMethod implements CallbackStrategyMethod {

    TaskScheduler scheduler;

    public AcceptDeleteScheduledTaskMethod(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        String taskName = callbackUpdate.replaceFirst(SchedulerCallbackData.SchedulerAcceptTaskDelete.toString(),"");

        if(MongoDB.deleteScheduledTask(chatId,taskName)) {
            scheduler.unScheduleTask(chatId, taskName);
            return new GetScheduledTasksMethod().getResponse(update, response, chatId);
        } else {
            return null;
        }

    }
}
