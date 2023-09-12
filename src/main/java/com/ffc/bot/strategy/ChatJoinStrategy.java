package main.java.com.ffc.bot.strategy;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.specialMessage.ChatJoinMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.CheckAuthoriseMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

public class ChatJoinStrategy implements Strategy {
    @Override
    public List<BotApiMethod> getResponse(Update update) {
        String chatId = String.valueOf(update.getMyChatMember().getChat().getId());
        if(!update.getMyChatMember().getOldChatMember().getStatus().equalsIgnoreCase("member")) {

            // adds a new closed queue to the db to avoid errors
            if(!MongoDB.chatRegistered(chatId)) {
                MongoDB.createNewQueue(chatId);
                MongoDB.updateField(MongoDB.QUEUE_STATE, QueueState.CLOSED.toString(), chatId);
            }

            List<BotApiMethod> methods = new ArrayList<>();

            ChatJoinMessage response = new ChatJoinMessage();

            response.setChatId(chatId);
            response.setText(DefaultQueueResponse.JOIN_CHAT_MESSAGE);

            methods.add(response);
            methods.addAll(new CheckAuthoriseMethod().getResponse(update,null,chatId));
            return methods;
        }
        return null;
    }
}
