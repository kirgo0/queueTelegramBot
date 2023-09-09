package main.java.com.ffc.bot.strategy;

import main.java.com.ffc.bot.*;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SwapResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.CheckAuthoriseMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.GetFirstAndNextQueueUsersMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class CallbackUpdateStrategy implements Strategy {
    @Override
    public List<BotApiMethod> getResponse(Update update) {

        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String userId = update.getCallbackQuery().getFrom().getId().toString();
        String callbackUpdate = update.getCallbackQuery().getData();

        if(!MongoDB.userExists(userId)) {
            MongoDB.createNewUser(userId, update.getCallbackQuery().getMessage().getFrom().getFirstName());
        } else {
            MongoDB.updateUser(userId, update.getCallbackQuery().getFrom().getFirstName());
        }

        StrategyMethod responseMethod;

        if(!MongoDB.userAuthorised(userId)) {
            if(!update.getCallbackQuery().getMessage().isReply()) {
                responseMethod = new CheckAuthoriseMethod();
                return responseMethod.getResponse(update,null, chatId);
            } else {
                return null;
            }
        }

        SendMessage response = new SendMessage();
        response.setChatId(chatId);

        String clickedMessageId = update.getCallbackQuery().getMessage().getMessageId().toString();

        // check if user clicked on empty button
        if(callbackUpdate.contains(CallbackData.QueueIn.toString())) {

            List<BotApiMethod> methods = new ArrayList<>();
            // get button position
            int queuePos = Integer.parseInt(callbackUpdate.replace(CallbackData.QueueIn.toString(),""));

            try {
                JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));
                boolean userInQueue = false;
                for (var user :
                     queue) {
                    if (user.equals(userId)) {
                        userInQueue = true;
                        break;
                    }
                }

                boolean userWasFirst = false;
                boolean userWasNext = false;

                String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
                if(userInQueue) {
                    if(QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(userId)) {
                        userWasFirst = true;
                    }
                    if(QueueCallModule.getNextQueueUser(queue).equalsIgnoreCase(userId)) {
                        userWasNext = true;
                    }
                    queue.put(queue.toList().indexOf(userId),MongoDB.EMPTY_QUEUE_MEMBER);
                }
                queue.put(queuePos,userId);

                MongoDB.updateQueue(queue.toString(),chatId);

                // Executing api method EditMessageReplyMarkup
                EditMessageReplyMarkup newResponse = new EditMessageReplyMarkup();

                newResponse.setChatId(chatId);
                newResponse.setMessageId(Integer.parseInt(clickedMessageId));

                // change message reply markup
                newResponse.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
                if(queueState.equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
                    boolean userNowFirst = QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(userId);
                    boolean userNowNext = QueueCallModule.getNextQueueUser(queue).equalsIgnoreCase(userId);
                    if ((!userWasFirst || !userNowFirst) && (!userWasNext || !userNowNext)) {
                        methods = new ArrayList<>(new GetFirstAndNextQueueUsersMethod(queue, true).getResponse(update,response,chatId));
                        return methods;
                    }
                }
                methods.add(newResponse);
                return methods;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(callbackUpdate.contains(CallbackData.QueueOut.toString())) {
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
                        return new GetFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId);
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

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(callbackUpdate.contains(CallbackData.SwapDenied.toString())) {
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

        if(callbackUpdate.contains(CallbackData.SwapAccepted.toString())) {
            String[] chatAndUserIds = callbackUpdate.split(CallbackData.SwapAccepted.toString());
            String firstUserId = chatAndUserIds[1], secondUserId = userId;
            chatId = chatAndUserIds[0];
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
            if(MongoDB.swapRequestExists(chatId,firstUserId,secondUserId)) {
                try {
                    JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));
                    boolean usersInQueue = queue.toList().contains(firstUserId) && queue.toList().contains(secondUserId);
                    if(usersInQueue) {
                        int[] usersPosInSwapRequest = MongoDB.getSwapRequestPositions(chatId, firstUserId, secondUserId);
                        int firstUserPos = queue.toList().indexOf(firstUserId), secondUserPos = queue.toList().indexOf(secondUserId);
                        // if nothing has changed from the moment the swap request was registered until now
                        if(usersPosInSwapRequest[0] == firstUserPos && usersPosInSwapRequest[1] == secondUserPos) {
                            List<BotApiMethod> methods = new ArrayList<>();
                            queue.put(firstUserPos,secondUserId);
                            queue.put(secondUserPos,firstUserId);

                            // if one of the users attempting the swap was in the first or next place
                            boolean usersWereFirstOrNext =
                                    (QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(firstUserId)
                                            || QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(secondUserId))
                                            ||
                                            (QueueCallModule.getNextQueueUser(queue).equalsIgnoreCase(firstUserId)
                                                    || QueueCallModule.getNextQueueUser(queue).equalsIgnoreCase(secondUserId));

                            MongoDB.updateQueue(queue.toString(),chatId);

                            if(usersWereFirstOrNext && MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
                                methods = new ArrayList<>(new GetFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId));
                            }

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
                    MongoDB.deleteSwapRequest(chatId, firstUserId,secondUserId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        }

        if(callbackUpdate.contains(CallbackData.CalledOut.toString())) {

            String[] chatAndUserIds = callbackUpdate.split(CallbackData.CalledOut.toString());
            userId = chatAndUserIds[1].trim();
            chatId = chatAndUserIds[0];

            response.setChatId(chatId);

            if(MongoDB.queueExists(chatId)) {

                PersonalSendMessage personalMessage = new PersonalSendMessage();
                personalMessage.setChatId(userId);

                if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {

                    JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

                    if(QueueCallModule.getFirstQueueUser(queue).equalsIgnoreCase(userId)) {
                        List<BotApiMethod> methods;

                        queue.remove(queue.toList().indexOf(userId));
                        queue.put(MongoDB.EMPTY_QUEUE_MEMBER);
                        MongoDB.updateQueue(queue.toString(),chatId);

                        methods = new ArrayList<>(new GetFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId));

                        personalMessage.setText(
                                new ResponseTextBuilder()
                                        .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                        .addTextLine()
                                        .addTextLine(CallQueueResponse.CALL_QUEUE_LEFT)
                                        .get()
                                );
                        methods.add(personalMessage);
                        return methods;
                    } else {
                        if(queue.toList().contains(userId)) {
                            personalMessage.setText(
                                    new ResponseTextBuilder()
                                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                            .addTextLine()
                                            .addTextLine(CallQueueResponse.ERROR_CAUSE_USER_IS_NO_LONGER_FIRST, TextFormat.Monocular)
                                            .get()
                            );
                        } else {
                            personalMessage.setText(
                                    new ResponseTextBuilder()
                                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                            .addTextLine()
                                            .addTextLine(CallQueueResponse.ERROR_CAUSE_USER_IS_NO_LONGER_IN_QUEUE, TextFormat.Monocular)
                                            .get()
                            );
                        }
                        return List.of(personalMessage);
                    }
                } else {
                    personalMessage.setText(
                            new ResponseTextBuilder()
                                    .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                                    .addTextLine()
                                    .addTextLine(CallQueueResponse.ERROR_CALL_QUEUE_IS_NOT_STARTED, TextFormat.Monocular)
                                    .get()
                    );
                    return List.of(personalMessage);
                }
            } else {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(DefaultQueueResponse.QUEUE_DOES_NOT_EXISTS, TextFormat.Bold)
                                .addTextLine()
                                .addTextLine(CallQueueResponse.ERROR_CALL_QUEUE_IS_NOT_STARTED, TextFormat.Monocular)
                                .get()
                );
                return List.of(response);
            }
        }

        if(callbackUpdate.contains(CallbackData.GetInfo.toString())) {
            callbackUpdate = callbackUpdate.replace(CallbackData.GetInfo.toString(),"");
            int lastMessageId = Integer.parseInt(MongoDB.getFieldValue(MongoDB.LAST_MESSAGE_ID,chatId));

            EditMessageText editMessage = new EditMessageText();
            editMessage.setMessageId(lastMessageId);
            try {
                int infoId = Integer.parseInt(callbackUpdate.trim());
                String info = MongoDB.getInfo(infoId);
                if(info != null) {
                    editMessage.setParseMode("HTML");
                    editMessage.setText(info);
                }
                editMessage.setChatId(chatId);

                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                List<InlineKeyboardButton> buttons = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(i+1));
                    StringBuilder sb = new StringBuilder();
                    sb.append(CallbackData.GetInfo).append(i);
                    button.setCallbackData(sb.toString());
                    buttons.add(button);
                }

                keyboard.add(buttons);

                editMessage.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
                return List.of(editMessage);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
