package main.java.com.ffc.bot.strategy.callbackStrategy.method.swap;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.markupConstructor.SwapResponseConstructor;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.SwapResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.CallbackData;
import main.java.com.ffc.bot.strategy.callbackStrategy.method.CallbackStrategyMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.NotifyFirstAndNextQueueUsersMethod;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class SwapAcceptedMethod implements CallbackStrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

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
                            methods = new ArrayList<>(new NotifyFirstAndNextQueueUsersMethod(queue).getResponse(update,response,chatId));
                        }

                        methods.addAll(0, SwapResponseConstructor.getSwapResponse(chatId, firstUserId, secondUserId, firstUserPos, secondUserPos));
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
        return null;
    }
}
