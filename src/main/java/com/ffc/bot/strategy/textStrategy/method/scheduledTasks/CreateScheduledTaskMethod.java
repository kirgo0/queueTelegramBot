package main.java.com.ffc.bot.strategy.textStrategy.method.scheduledTasks;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.markupConstructor.ScheduledTaskMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SchedulerResponse;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CreateScheduledTaskMethod implements TextStrategyMethod {

    TaskScheduler scheduler;
    String name;

    public CreateScheduledTaskMethod(TaskScheduler scheduler, String name) {
        this.scheduler = scheduler;
        this.name = name;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {

        if(name.equalsIgnoreCase("")) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(SchedulerResponse.SCHEDULER_NO_PARAM_ACCEPT, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.CREATE_SCHEDULED_TASK, TextFormat.Bold)
                            .get()
            );

            return List.of(response);
        }

        if(scheduler != null) {
            if(MongoDB.getScheduledTask(chatId, name) != null) {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(SchedulerResponse.SCHEDULER_TASK_NAME_IS_ALREADY_TAKEN, TextFormat.Monocular)
                                .addTextLine()
                                .startFormat(TextFormat.Bold)
                                .addTextLine(BotCommandsResponse.GET_SCHEDULED_TASKS)
                                .addTextLine(BotCommandsResponse.CREATE_SCHEDULED_TASK)
                                .endFormat(TextFormat.Bold)
                                .get()
                );
                return List.of(response);
            }

            MongoDB.createNewScheduledTask(chatId,name);
            var task = MongoDB.getScheduledTask(chatId, name);

            if(task != null) {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(SchedulerResponse.SCHEDULER_TITLE,TextFormat.Bold)
                                .addTextLine()
                                .addTextLine(SchedulerResponse.SCHEDULER_CREATED_NEW_TASK, TextFormat.Italic).addText(task.getName())
                                .addTextLine()
                                .addTextLine(SchedulerResponse.NEXT_QUEUE_CREATION_TIME_IS).addText(TaskScheduler.getRemainingTime(task))
                                .get()
                );
                response.setReplyMarkup(ScheduledTaskMarkupConstructor.getSingleTaskMarkup(task));

                scheduler.scheduleTasks(List.of(task));
                return List.of(response);
            } else {
                return null;
            }
        } else {
            System.out.println("No Task Scheduler registered in system");
        }
        return null;
    }
}
