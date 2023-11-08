package main.java.com.ffc.bot.strategy.callbackStrategy.method.sheduledTasks;

import main.java.com.ffc.bot.markupConstructor.ScheduledTaskMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SchedulerResponse;
import main.java.com.ffc.bot.scheduler.ScheduledTask;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class UpdateTimeForTaskMethod {

    TaskScheduler scheduler;

    public UpdateTimeForTaskMethod(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public List<BotApiMethod> getResponse(String clickedMessageId, String taskName, String chatId, ScheduledTask task) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setParseMode("HTML");

        newMessage.setChatId(chatId);
        newMessage.setMessageId(Integer.parseInt(clickedMessageId));

        newMessage.setText(
                new ResponseTextBuilder()
                        .addText(SchedulerResponse.SCHEDULER_TITLE, TextFormat.Bold)
                        .addTextLine()
                        .addTextLine(SchedulerResponse.SCHEDULER_SELECTED_QUEUE).addText(taskName)
                        .addTextLine()
                        .addTextLine(SchedulerResponse.NEXT_QUEUE_CREATION_TIME_IS).addText(TaskScheduler.getRemainingTime(task), TextFormat.Italic)
                        .get()
        );
        newMessage.setReplyMarkup(ScheduledTaskMarkupConstructor.getSingleTaskMarkup(task));

        scheduler.rescheduleTask(task);
        return List.of(newMessage);
    }

}
