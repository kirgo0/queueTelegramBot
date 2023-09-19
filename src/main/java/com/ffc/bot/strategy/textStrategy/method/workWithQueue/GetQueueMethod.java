package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.CallQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class GetQueueMethod implements TextStrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE,chatId);
        if(queueState.equalsIgnoreCase(QueueState.QUEUE_STARTED.toString())) {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(CallQueueResponse.CALL_QUEUE_TITLE, TextFormat.Bold)
                            .addTextLine()
                            .addTextLine(CallQueueResponse.DEFAULT_RESPONSE)
                            .get()
            );
        }
        response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
        return List.of(response);
    }
}
