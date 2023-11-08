package main.java.com.ffc.bot.strategy.callbackStrategy.method.sheduledTasks;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.markupConstructor.ScheduledTaskMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SchedulerResponse;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.strategy.SchedulerCallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class ChangeQueueSizeForTaskMethod implements CallbackStrategyMethod {

    String clickedMessageId;

    public ChangeQueueSizeForTaskMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        boolean isPositive = callbackUpdate.contains(SchedulerCallbackData.SchedulerPlusQueueSize.toString());

        String[] params;
        if(isPositive) {
            params = callbackUpdate.split(SchedulerCallbackData.SchedulerPlusQueueSize.toString());
        } else {
            params = callbackUpdate.split(SchedulerCallbackData.SchedulerMinusQueueSize.toString());
        }

        if(params.length != 2) return null;

        var count = Integer.parseInt(params[0]);
        var taskName = params[1];

        var task = MongoDB.getScheduledTask(chatId,taskName);
        if(task == null) return null;

        EditMessageText newMessage = new EditMessageText();
        newMessage.setParseMode("HTML");

        newMessage.setChatId(chatId);
        newMessage.setMessageId(Integer.parseInt(clickedMessageId));

        var textBuilder = new ResponseTextBuilder()
                .addText(SchedulerResponse.SCHEDULER_TITLE, TextFormat.Bold)
                .addTextLine()
                .addTextLine(SchedulerResponse.SCHEDULER_SELECTED_QUEUE).addText(taskName);

        if(isPositive && task.getQueueSize() + count <= 100) {
            task.setQueueSize(task.getQueueSize() + count);
        } else if(!isPositive && task.getQueueSize() - count > 1) {
            task.setQueueSize(task.getQueueSize() - count);
        } else if (isPositive) {
            textBuilder.addTextLine().addTextLine(SchedulerResponse.SCHEDULER_MAX_QUEUE_SIZE_REACHED, TextFormat.Monocular);
        }  else {
            textBuilder.addTextLine().addTextLine(SchedulerResponse.SCHEDULER_MIN_QUEUE_SIZE_REACHED, TextFormat.Monocular);
        }
        MongoDB.updateScheduledTask(chatId,taskName, task);
        newMessage.setText(textBuilder
                .addTextLine()
                .addTextLine(SchedulerResponse.NEXT_QUEUE_CREATION_TIME_IS).addText(TaskScheduler.getRemainingTime(task))
                .get()
        );
        newMessage.setReplyMarkup(ScheduledTaskMarkupConstructor.getSingleTaskMarkup(task));
        return List.of(newMessage);

    }
}
