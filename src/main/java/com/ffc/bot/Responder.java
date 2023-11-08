package main.java.com.ffc.bot;

import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.specialMessage.AuthoriseMessage;
import main.java.com.ffc.bot.specialMessage.ChatJoinMessage;
import main.java.com.ffc.bot.strategy.callbackStrategy.CallbackUpdateStrategy;
import main.java.com.ffc.bot.strategy.textStrategy.TextUpdateStrategy;
import main.java.com.ffc.bot.strategy.ChatJoinStrategy;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;

public class Responder extends TelegramLongPollingBot {

    private TaskScheduler scheduler;

    @Override
    public String getBotUsername() {
        return PropertiesReader.getProperty("bot_name");
    }

    @Override
    public String getBotToken() {
        return PropertiesReader.getProperty("bot_token");
    }

    @Override
    public void onUpdateReceived(Update update) {

        List<BotApiMethod> methods = null;

        if(update.hasMyChatMember()) {
            methods = new ChatJoinStrategy().getResponse(update);
        }
        if(update.hasMessage()) {
            methods = new TextUpdateStrategy(scheduler).getResponse(update);
        }
        if(update.hasCallbackQuery()) {
            methods = new CallbackUpdateStrategy(scheduler).getResponse(update);
        }

        if(methods == null) return;

        SendMessages(methods);
    }

    public void SendMessages(List<BotApiMethod> methods) {
        for (BotApiMethod method :
                methods) {
            try {
                // if the bot send message it deletes the previous message whose id value was obtained from the DB
                try {
                    // if the user requests textMessage, finds the last sent message and deletes it
                    if(method.getClass().equals(ChatJoinMessage.class)) {
                        String chatId = ((SendMessage) method).getChatId();
                    } else if(method.getClass().equals(SendMessage.class)) {
                        String chatId = ((SendMessage) method).getChatId();
                        if(!MongoDB.getFieldValue(MongoDB.LAST_MESSAGE_ID,chatId).equalsIgnoreCase("" )) {
                            DeleteMessage deleteMessage = new DeleteMessage(chatId, Integer.parseInt(MongoDB.getFieldValue(MongoDB.LAST_MESSAGE_ID,chatId)));
                            sendApiMethod(deleteMessage);
                        }
                        // if the user requests authorise message, finds the last authorise message and deletes it
                    } else if(method.getClass().equals(AuthoriseMessage.class)) {
                        String chatId = ((SendMessage) method).getChatId();
                        if(!MongoDB.getFieldValue(MongoDB.LAST_AUTHORISE_MESSAGE_ID,chatId).equalsIgnoreCase("")) {
                            DeleteMessage deleteMessage = new DeleteMessage(chatId, Integer.parseInt(MongoDB.getFieldValue(MongoDB.LAST_AUTHORISE_MESSAGE_ID,chatId)));
                            sendApiMethod(deleteMessage);
                        }
                    }
                } catch (TelegramApiException e1) {
                    e1.printStackTrace();
                }
                // updating last messages id
                Message message = (Message) sendApiMethod(method);
                if(method.getClass().equals(SendMessage.class)) {
                    MongoDB.updateField(MongoDB.LAST_MESSAGE_ID,message.getMessageId().toString(),message.getChatId().toString());
                } else if(method.getClass().equals(AuthoriseMessage.class)) {
                    MongoDB.updateField(MongoDB.LAST_AUTHORISE_MESSAGE_ID,message.getMessageId().toString(),message.getChatId().toString());
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void SendScheduledMessage(SendMessage method) {
        try {
            sendApiMethod(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void registerScheduler(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }
}
