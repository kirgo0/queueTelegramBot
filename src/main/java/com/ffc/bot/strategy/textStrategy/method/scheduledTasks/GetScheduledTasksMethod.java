package main.java.com.ffc.bot.strategy.textStrategy.method.scheduledTasks;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.markupConstructor.ScheduledTaskMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SchedulerResponse;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public class GetScheduledTasksMethod implements TextStrategyMethod, CallbackStrategyMethod {

    String clickedMessageId;

    public GetScheduledTasksMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    public GetScheduledTasksMethod() {

    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {

        var taskList = MongoDB.getScheduledTasks(chatId);
        if(taskList != null) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(SchedulerResponse.SCHEDULER_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(SchedulerResponse.SCHEDULED_QUEUES_LIST)
                            .get()
            );
            response.setReplyMarkup(ScheduledTaskMarkupConstructor.getAllTasksMarkup(taskList));
        } else {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(SchedulerResponse.SCHEDULER_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(SchedulerResponse.SCHEDULED_QUEUES_LIST_IS_EMPTY)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.CREATE_SCHEDULED_TASK,TextFormat.Bold)
                            .get()
            );
        }
        return List.of(response);
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {
        var result = getResponse(update,response, chatId);

        EditMessageText newMessage = new EditMessageText();
        newMessage.setParseMode("HTML");

        newMessage.setChatId(chatId);
        newMessage.setMessageId(Integer.parseInt(clickedMessageId));

        response = (SendMessage) result.get(0);

        newMessage.setText(response.getText());
        newMessage.setReplyMarkup((InlineKeyboardMarkup) response.getReplyMarkup());
        return List.of(newMessage);
    }
}
