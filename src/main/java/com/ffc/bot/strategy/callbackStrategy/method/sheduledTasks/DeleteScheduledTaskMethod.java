package main.java.com.ffc.bot.strategy.callbackStrategy.method.sheduledTasks;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SchedulerResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.SchedulerCallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class DeleteScheduledTaskMethod implements CallbackStrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        String taskName = callbackUpdate.replaceFirst(SchedulerCallbackData.SchedulerTaskDelete.toString(),"");

        response.setText(
                new ResponseTextBuilder()
                        .addText(MongoDB.getUserName(userId) + ",")
                        .addTextLine(SchedulerResponse.DELETE_SCHEDULED_TASK_AUTH)
                        .addText("\"" + taskName + "\"?", TextFormat.Italic)
                        .get()
        );

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton acceptButton = new InlineKeyboardButton();
        acceptButton.setText(ButtonsText.ACCEPT_ACTION);
        StringBuilder sb = new StringBuilder();
        sb.append(SchedulerCallbackData.SchedulerAcceptTaskDelete).append(taskName);
        acceptButton.setCallbackData(sb.toString());

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(ButtonsText.BACK_IN_MENU);
        sb = new StringBuilder();
        sb.append(SchedulerCallbackData.SchedulerGet).append(taskName);
        backButton.setCallbackData(sb.toString());

        keyboard.add(List.of(backButton, acceptButton));
        response.setReplyMarkup(new InlineKeyboardMarkup(keyboard));

        return List.of(response);
    }
}
