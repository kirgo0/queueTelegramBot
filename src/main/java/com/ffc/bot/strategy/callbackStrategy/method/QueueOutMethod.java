package main.java.com.ffc.bot.strategy.callbackStrategy.method;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SwapResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.NotifyFirstAndNextQueueUsersMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class QueueOutMethod implements CallbackStrategyMethod {


    private String clickedMessageId;

    public QueueOutMethod(String clickedMessageId) {
        this.clickedMessageId = clickedMessageId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {
        int queuePos = Integer.parseInt(callbackUpdate.replace(CallbackData.QueueOut.toString(),""));

        try {
            JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));
            if(queue.get(queuePos).equals(userId)) {

                queue.put(queuePos,MongoDB.EMPTY_QUEUE_MEMBER);

                MongoDB.updateQueue(queue.toString(),chatId);

                // Executing api method EditMessageReplyMarkup
                EditMessageReplyMarkup newResponse = new EditMessageReplyMarkup();

                newResponse.setChatId(chatId);
                newResponse.setMessageId(Integer.parseInt(clickedMessageId));

                // change message reply markup
                newResponse.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));

                if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
                    return new NotifyFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId);
                }
                return List.of(newResponse);
            }

            // check if user clicked on other user, this means they want to swap
            if(queue.toList().contains(userId)) {
                // get both user ids
                String firstUserId = (String) queue.get(queue.toList().indexOf(userId));
                String secondUserId = (String) queue.get(queuePos);

                // check if swap request between both users already exists
                if(!MongoDB.swapRequestExists(chatId,firstUserId,secondUserId) && !MongoDB.swapRequestExists(chatId,secondUserId,firstUserId)) {
                    MongoDB.createNewSwapRequest(chatId,firstUserId,secondUserId,queue.toList().indexOf(userId),queuePos);
                }
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

                InlineKeyboardButton acceptSwapButton = new InlineKeyboardButton(ButtonsText.ACCEPT_SWAP_REQUEST);

                sb = new StringBuilder();
                sb.append(chatId).append(CallbackData.SwapAccepted).append(firstUserId);

                acceptSwapButton.setCallbackData(sb.toString());
                secondUserKeyboard.add(List.of(acceptSwapButton));

                denySwapButton = new InlineKeyboardButton(ButtonsText.DENY_SWAP_REQUEST);

                sb = new StringBuilder();
                sb.append(chatId).append(CallbackData.SwapDenied).append(firstUserId);
                denySwapButton.setCallbackData(sb.toString());
                secondUserKeyboard.add(List.of(denySwapButton));

                markup = new InlineKeyboardMarkup(secondUserKeyboard);
                messageToSecondUser.setReplyMarkup(markup);
                return List.of(messageToFirstUser,messageToSecondUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
