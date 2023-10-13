package main.java.com.ffc.bot.markupConstructor;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SwapResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;

import java.util.ArrayList;
import java.util.List;

public class SwapRequestConstructor {

    public static List<BotApiMethod> getSwapRequests(String chatId, String firstUserId, String secondUserId, int firstUserPos, int secondUserPos) {
        List<BotApiMethod> methods = new ArrayList<>();

        EditMessageReplyMarkup newResponse = new EditMessageReplyMarkup();

        newResponse.setChatId(chatId);
        newResponse.setMessageId(Integer.parseInt(MongoDB.getFieldValue(MongoDB.LAST_MESSAGE_ID,chatId)));

        // change message reply markup
        newResponse.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));

        methods.add(newResponse);

        MongoDB.deleteSwapRequest(chatId, firstUserId,secondUserId);

        StringBuilder sb = new StringBuilder();

        PersonalSendMessage firstUserMessage = new PersonalSendMessage();

        firstUserMessage.setText(
                new ResponseTextBuilder()
                        .addText(SwapResponse.PERSONAL_REQUEST_TITLE, TextFormat.Bold)
                        .addTextLine()
                        .startFormat(TextFormat.Italic)
                        .addTextLine(SwapResponse.SWAP_REQUEST_SUCCEED)
                        .addText(MongoDB.getUserName(secondUserId))
                        .addTextLine()
                        .addTextLine(SwapResponse.USER_WHO_GOT_REQUEST_PLACE)
                        .addText(SwapResponse.BETWEEN_TEXT_AND_NUMBER)
                        .addText(String.valueOf(secondUserPos))
                        .endFormat(TextFormat.Italic)
                        .get()
        );
        firstUserMessage.setChatId(firstUserId);

        PersonalSendMessage secondUserMessage = new PersonalSendMessage();

        secondUserMessage.setText(
                new ResponseTextBuilder()
                        .addText(SwapResponse.PERSONAL_REQUEST_TITLE, TextFormat.Bold)
                        .addTextLine()
                        .startFormat(TextFormat.Italic)
                        .addTextLine(SwapResponse.SWAP_REQUEST_SUCCEED)
                        .addText(MongoDB.getUserName(firstUserId))
                        .addTextLine()
                        .addTextLine(SwapResponse.USER_WHO_GOT_REQUEST_PLACE)
                        .addText(SwapResponse.BETWEEN_TEXT_AND_NUMBER)
                        .addText(String.valueOf(firstUserPos))
                        .endFormat(TextFormat.Italic)
                        .get());
        secondUserMessage.setChatId(secondUserId);

        methods.add(0,firstUserMessage);
        methods.add(0,secondUserMessage);

        return methods;
    }
}
