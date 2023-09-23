package main.java.com.ffc.bot.strategy.callbackStrategy.method.swap;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SwapResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class SwapDeniedMethod implements CallbackStrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

        String[] chatAndUserIds = callbackUpdate.split(CallbackData.SwapDenied.toString());
        chatId = chatAndUserIds[0];
        String firstUserId = "", secondUserId = "";
        if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.CLOSED.toString())) {
            PersonalSendMessage personalSendMessage = new PersonalSendMessage();
            personalSendMessage.setText(
                    new ResponseTextBuilder()
                            .addText(SwapResponse.PERSONAL_REQUEST_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(SwapResponse.ERROR_CAUSE_QUEUE_IS_CLOSED, TextFormat.Monocular)
                            .get()
            );
            personalSendMessage.setChatId(userId);
            return List.of(personalSendMessage);
        }

        if(MongoDB.swapRequestExists(chatAndUserIds[0],chatAndUserIds[1],userId)) {
            MongoDB.deleteSwapRequest(chatAndUserIds[0],chatAndUserIds[1],userId);
            firstUserId = chatAndUserIds[1];
            secondUserId = userId;
        } else if(MongoDB.swapRequestExists(chatAndUserIds[0],userId,chatAndUserIds[1])) {
            MongoDB.deleteSwapRequest(chatAndUserIds[0],userId,chatAndUserIds[1]);
            firstUserId = userId;
            secondUserId = chatAndUserIds[1];
        } else {
            PersonalSendMessage newMessage = new PersonalSendMessage();
            newMessage.setChatId(userId);
            newMessage.setText(
                    new ResponseTextBuilder()
                            .addText(SwapResponse.PERSONAL_REQUEST_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(SwapResponse.ERROR_CAUSE_REQUEST_NO_LONGER_RELEVANT,TextFormat.Monocular).get()
            );
            return List.of(newMessage);
        }

        String message = new ResponseTextBuilder()
                .addText(SwapResponse.PERSONAL_REQUEST_TITLE, TextFormat.Bold)
                .addTextLine()
                .addTextLine(SwapResponse.REQUEST_WAS_CLOSED, TextFormat.Italic)
                .get();

        PersonalSendMessage firstUserMessage = new PersonalSendMessage();
        firstUserMessage.setText(message);
        firstUserMessage.setChatId(firstUserId);

        PersonalSendMessage secondUserMessage = new PersonalSendMessage();
        secondUserMessage.setText(message);
        secondUserMessage.setChatId(secondUserId);

        return List.of(firstUserMessage,secondUserMessage);
    }
}
