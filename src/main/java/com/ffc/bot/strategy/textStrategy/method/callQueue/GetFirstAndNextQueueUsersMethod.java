package main.java.com.ffc.bot.strategy.textStrategy.method.callQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class GetFirstAndNextQueueUsersMethod implements StrategyMethod {

    private JSONArray queue;
    private boolean queueChanged = false;

    public GetFirstAndNextQueueUsersMethod(JSONArray queue) {
        this.queue = queue;
    }

    public GetFirstAndNextQueueUsersMethod(JSONArray queue, boolean queueChanged) {
        this.queue = queue;
        this.queueChanged = queueChanged;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        response.setParseMode("HTML");

        // if there is no one in the queue, exits the call queue process
        if(QueueCallModule.queueEmpty(queue)) {
            MongoDB.updateField(MongoDB.QUEUE_STATE, QueueState.CLOSED.toString(),chatId);

            response.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.CALL_QUEUE_FINISHED, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(BotCommandsResponse.CREATE_QUEUE, TextFormat.Bold)
                            .get()
            );
            return List.of(response);
        }

        // if the queue is not empty
        String firstUserId = QueueCallModule.getFirstQueueUser(queue);
        String nextUserId = QueueCallModule.getNextQueueUser(queue);

        PersonalSendMessage messageFirstUser = new PersonalSendMessage();
        messageFirstUser.setChatId(firstUserId);

        // if something has changed in the queue
        if (queueChanged) {
            messageFirstUser.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.QUEUE_ORDER_CHANGED, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.FIRST_QUEUE_MEMBER_BODY)
                            .get()
            );
        } else {
            messageFirstUser.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.FIRST_QUEUE_MEMBER_BODY)
                            .get()
            );
        }

        // if something has changed in the queue
        InlineKeyboardButton button = new InlineKeyboardButton(ButtonsText.LEAVE_CALL_QUEUE);
        StringBuilder sb = new StringBuilder();
        sb
                .append(chatId)
                .append(CallbackData.CalledOut)
                .append(firstUserId);
        button.setCallbackData(sb.toString());

        messageFirstUser.setReplyMarkup(new InlineKeyboardMarkup(
                List.of(List.of(button))
        ));

        // creates a button for the next user in the queue
        PersonalSendMessage messageNextUser = new PersonalSendMessage();
        messageNextUser.setChatId(nextUserId);

        // checking if something in the queue has changed
        if(!queueChanged) {
            messageNextUser.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.NEXT_QUEUE_MEMBER_BODY)
                            .get()
            );
        } else {
            messageNextUser.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.QUEUE_ORDER_CHANGED, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.NEXT_QUEUE_MEMBER_BODY)
                            .get()
            );
        }

        response.setText(
                new ResponseTextBuilder()
                        .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                        .addTextLine()
                        .addTextLine(CallQueueResponse.DEFAULT_RESPONSE)
                        .get()
                );


        response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
        // if there is only one user in the queue
        if(messageNextUser.getChatId().equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) return List.of(messageFirstUser, response);
        // if there is two or more users in the queue
        return List.of(messageFirstUser, messageNextUser, response);
    }
}
