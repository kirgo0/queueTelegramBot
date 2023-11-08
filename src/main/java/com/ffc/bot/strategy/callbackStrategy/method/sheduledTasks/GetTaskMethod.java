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

public class GetTaskMethod implements CallbackStrategyMethod {

    String clickedMessageId;

    public GetTaskMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {
        var taskName = callbackUpdate.replaceFirst(SchedulerCallbackData.SchedulerGet.toString(), "");

        EditMessageText newMessage = new EditMessageText();
        newMessage.setParseMode("HTML");

        newMessage.setChatId(chatId);
        newMessage.setMessageId(Integer.parseInt(clickedMessageId));

        var task = MongoDB.getScheduledTask(chatId,taskName);
        if(task != null) {
            newMessage.setText(
                    new ResponseTextBuilder()
                            .addText(SchedulerResponse.SCHEDULER_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(SchedulerResponse.SCHEDULER_SELECTED_QUEUE).addText(taskName)
                            .addTextLine()
                            .addTextLine(SchedulerResponse.NEXT_QUEUE_CREATION_TIME_IS).addText(TaskScheduler.getRemainingTime(task))
                            .get()
            );
            newMessage.setReplyMarkup(ScheduledTaskMarkupConstructor.getSingleTaskMarkup(task));
            return List.of(newMessage);
        }
        return null;
    }
}
