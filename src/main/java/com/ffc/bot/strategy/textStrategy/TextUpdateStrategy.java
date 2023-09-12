package main.java.com.ffc.bot.strategy.textStrategy;

import main.java.com.ffc.bot.*;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.Strategy;
import main.java.com.ffc.bot.strategy.textStrategy.method.*;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.SkipQueueMemberMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.StartCallQueueMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.callQueue.StopCallQueueMethod;
import main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class TextUpdateStrategy implements Strategy {
    @Override
    public List<BotApiMethod> getResponse(Update update) {

        String chatId = update.getMessage().getChatId().toString();
        String userId = update.getMessage().getFrom().getId().toString();
        String textUpdate = update.getMessage().getText().toLowerCase().trim();

        // Adds user into database
        if(!MongoDB.userExists(userId)) {
            MongoDB.createNewUser(userId, update.getMessage().getFrom().getFirstName());
        } else {
            MongoDB.updateUser(userId, update.getMessage().getFrom().getFirstName());
        }

        String bot_name = PropertiesReader.getProperty("bot_name").toLowerCase();

        // clears command from @bot_name
        if(textUpdate.contains(bot_name)) {
            StringBuilder sb = new StringBuilder();
            sb.append("@");
            sb.append(bot_name);
            textUpdate = textUpdate.replace(sb.toString(),"");
        }

        // default response init
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText(DefaultQueueResponse.CHOOSE_A_PLACE);
        response.setParseMode("HTML");

        // authorising user
        if(textUpdate.equalsIgnoreCase("/start") && !MongoDB.userAuthorised(userId)) {
            MongoDB.authoriseUser(userId,true);
            PersonalSendMessage message = new PersonalSendMessage();
            message.setChatId(userId);
            message.setText(
                    new ResponseTextBuilder()
                            .addText(DefaultQueueResponse.AUTHORISED_MESSAGE_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(DefaultQueueResponse.AUTHORISED_MESSAGE_BODY, TextFormat.Italic)
                            .get()
            );
            return List.of(message);
        }

        StrategyMethod responseMethod = null;

        // checks if user is authorised
        if(!MongoDB.userAuthorised(userId)) {
            // checks if user is not replying to bot messages
            if(!update.getMessage().isReply()) {
                responseMethod = new CheckAuthoriseMethod();
                return responseMethod.getResponse(update,response,chatId);
            } else {
                return null;
            }
        }

        // check if user send command in personal messages
        if (!(chatId.toCharArray()[0] == '-')) {
            responseMethod = new MessageIsPersonalMethod(userId);
            return responseMethod.getResponse(update, null, chatId);
        }

        // methods that do not have a built-in check for a queue existing
        List<String> needsExistingQueueMethods = List.of(
                "/openqueue",
                "/closequeue",
                "/getqueue",
                "/startcallqueue",
                "/stopcallqueue",
                "/skipqueuemember"
        );

        if(needsExistingQueueMethods.contains(textUpdate)) {
            if(MongoDB.queueExists(chatId)) {
                // methods that do not have a built-in check for a closed queue
                List<String> needsNotClosedQueueMethods = List.of(
                        "/closequeue",
                        "/getqueue",
                        "/startcallqueue",
                        "/stopcallqueue",
                        "/skipqueuemember"
                );
                if(needsNotClosedQueueMethods.contains(textUpdate)) {
                    // checking if queue is not closed
                    if(!MongoDB.getFieldValue(MongoDB.QUEUE_STATE, chatId).equalsIgnoreCase(QueueState.CLOSED.toString())) {
                        if(textUpdate.equalsIgnoreCase("/closeQueue")) {
                            responseMethod = new CloseQueueMethod();
                        } else if(textUpdate.equalsIgnoreCase("/getQueue")) {
                            responseMethod = new GetQueueMethod();
                        } else if(textUpdate.equalsIgnoreCase("/startCallQueue")){
                            responseMethod = new StartCallQueueMethod();
                        } else if(textUpdate.equalsIgnoreCase("/stopCallQueue")){
                            responseMethod = new StopCallQueueMethod();
                        } else if(textUpdate.equalsIgnoreCase("/skipQueueMember")){
                            responseMethod = new SkipQueueMemberMethod();
                        }
                    } else {
                        responseMethod = new QueueIsNotOpenMethod();
                    }
                } else {
                    // methods that can be executed only with a closed queue
                    if(textUpdate.equalsIgnoreCase("/openQueue")) {
                        responseMethod = new OpenQueueMethod();
                    }
                }
            } else {
                responseMethod = new QueueDoesNotExistDefaultMethod();
            }
        } else {
            if(textUpdate.equalsIgnoreCase("/help")){
                responseMethod = new GetInfoMethod();
            } else if(textUpdate.equalsIgnoreCase("/createQueue")) {
                responseMethod = new CreateQueueMethod();
            } else if(textUpdate.contains("/createqueue")) {
                // TODO
//            if(MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId).equalsIgnoreCase(QueueState.CLOSED.toString())) {
//                textUpdate = textUpdate.replace("/createQueue","/setdqs");
//            }
//            responseMethod = new SetDefaultQueueSizeMethod(response, textUpdate);
            } else if(textUpdate.contains("/setdqs")) {
                responseMethod = new SetDefaultQueueSizeMethod(textUpdate);
            } else if(textUpdate.equalsIgnoreCase("/changeQueueView")) {
                responseMethod = new ChangeQueueViewMethod();
            }
        }

        if(responseMethod != null) return responseMethod.getResponse(update,response,chatId);
        else return null;
    }
}
