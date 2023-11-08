package main.java.com.ffc.bot.strategy;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SchedulerResponse;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue.CreateQueueMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class AutoCreateQueueMethod implements AutoStrategyMethod {

    TaskScheduler scheduler;

    public AutoCreateQueueMethod(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public List<BotApiMethod> getResponse(String chatId, String name) {

        SendMessage response = new SendMessage();
        response.setChatId(chatId);

        response.setParseMode("HTML");

        var task = MongoDB.getScheduledTask(chatId,name);

        if(task != null) {
            String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
            if(queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(SchedulerResponse.SCHEDULER_TITLE, TextFormat.Bold)
                                .addTextLine()
                                .addTextLine(SchedulerResponse.SCHEDULER_CREATED_QUEUE).addText(name)
                                .get()
                );

            } else {
                var rtb = new ResponseTextBuilder()
                        .addText(SchedulerResponse.SCHEDULER_TITLE, TextFormat.Bold)
                        .addTextLine()
                        .addTextLine(SchedulerResponse.SCHEDULER_CREATED_QUEUE).addText(name);

                if(!QueueCallModule.queueEmpty(new JSONArray(MongoDB.getQueue(chatId)))) {
                    if(MongoDB.savedQueueExists(chatId, MongoDB.DEFAULT_SAVED_QUEUE_NAME)) {
                        MongoDB.updateDefaultSavedQueue(chatId);
                    } else {
                        MongoDB.createNewDefaultSavedQueue(chatId);
                    }
                    rtb
                            .addTextLine()
                            .addTextLine(SchedulerResponse.SCHEDULER_SAVED_LAST_QUEUE, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.GET_SAVED_QUEUES,TextFormat.Bold)
                    ;
                }

                response.setText(rtb.get());

            }
            MongoDB.updateField(MongoDB.DEFAULT_QUEUE_SIZE,String.valueOf(task.getQueueSize()),chatId);
            MongoDB.updateField(MongoDB.QUEUE_STATE, QueueState.CLOSED.toString(), chatId);
            new CreateQueueMethod().getResponse(new Update(),new SendMessage(), chatId);

            response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
            scheduler.rescheduleTask(task);
            return List.of(response);
        }
        return null;

    }
}
