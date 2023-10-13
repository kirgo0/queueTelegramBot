package main.java.com.ffc.bot.strategy.callbackStrategy.method;

import main.java.com.ffc.bot.markupConstructor.InfoMarkupConstructor;
import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.strategy.CallbackData;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CallbackGetInfoMethod implements CallbackStrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate) {

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

            editMessage.setReplyMarkup(InfoMarkupConstructor.getMarkup());
            return List.of(editMessage);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
