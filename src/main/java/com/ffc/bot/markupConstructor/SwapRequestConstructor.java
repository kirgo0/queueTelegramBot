package main.java.com.ffc.bot.markupConstructor;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SwapResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.strategy.CallbackData;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class SwapRequestConstructor {

    public static List<BotApiMethod> getSwapRequest(String chatId, String firstUserId, String secondUserId, String userId, JSONArray queue, int queuePos) {
        // build first user personal response
        PersonalSendMessage messageToFirstUser = new PersonalSendMessage();
        messageToFirstUser.setChatId(firstUserId);

        messageToFirstUser.setText(
                new ResponseTextBuilder()
                        .addText(SwapResponse.PERSONAL_REQUEST_TITLE, TextFormat.Bold)
                        .addTextLine()
                        .addTextLine(MongoDB.getUserName(secondUserId))
                        .addText(SwapResponse.USER_WHO_SEND_REQUEST_END)
                        .get()
        );

        List<List<InlineKeyboardButton>> firstUserKeyboard = new ArrayList<>();

        InlineKeyboardButton denySwapButton = new InlineKeyboardButton(ButtonsText.DENY_SWAP_REQUEST);

        StringBuilder sb = new StringBuilder();
        sb.append(chatId).append(CallbackData.SwapDenied).append(secondUserId);
        denySwapButton.setCallbackData(sb.toString());
        firstUserKeyboard.add(List.of(denySwapButton));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(firstUserKeyboard);
        messageToFirstUser.setReplyMarkup(markup);

        // build second user personal response
        PersonalSendMessage messageToSecondUser = new PersonalSendMessage();
        messageToSecondUser.setChatId(secondUserId);

        String firstUsername = MongoDB.getUserName(firstUserId);
        messageToSecondUser.setText(
                new ResponseTextBuilder()
                        .addText(SwapResponse.PERSONAL_REQUEST_TITLE, TextFormat.Bold)
                        .addTextLine()
                        .addTextLine(firstUsername)
                        .addText(SwapResponse.USER_WHO_GOT_REQUEST_END)
                        .addTextLine()
                        .startFormat(TextFormat.Italic)
                        .addTextLine(SwapResponse.USER_WHO_GOT_REQUEST_PLACE)
                        .addText(SwapResponse.BETWEEN_TEXT_AND_NUMBER)
                        .addText(String.valueOf(queuePos))
                        .addTextLine(SwapResponse.USER_WHO_SEND_REQUEST_PLACE)
                        .addText(firstUsername)
                        .addText(SwapResponse.BETWEEN_TEXT_AND_NUMBER)
                        .addText(String.valueOf(queue.toList().indexOf(userId)))
                        .endFormat(TextFormat.Italic)
                        .get()
        );

        List<List<InlineKeyboardButton>> secondUserKeyboard = new ArrayList<>();

        InlineKeyboardButton acceptSwapButton = new InlineKeyboardButton(
                new ResponseTextBuilder().addText(ButtonsText.ACCEPT_SWAP_REQUEST).addText(ButtonsText.ACCEPT_ACTION).get()
        );

        sb = new StringBuilder();
        sb.append(chatId).append(CallbackData.SwapAccepted).append(firstUserId);

        acceptSwapButton.setCallbackData(sb.toString());
        secondUserKeyboard.add(List.of(acceptSwapButton));

        denySwapButton = new InlineKeyboardButton(
                new ResponseTextBuilder().addText(ButtonsText.ACCEPT_SWAP_REQUEST).addText(ButtonsText.DENY_ACTION).get()
        );

        sb = new StringBuilder();
        sb.append(chatId).append(CallbackData.SwapDenied).append(firstUserId);
        denySwapButton.setCallbackData(sb.toString());
        secondUserKeyboard.add(List.of(denySwapButton));

        markup = new InlineKeyboardMarkup(secondUserKeyboard);
        messageToSecondUser.setReplyMarkup(markup);
        return List.of(messageToFirstUser,messageToSecondUser);
    }
}
