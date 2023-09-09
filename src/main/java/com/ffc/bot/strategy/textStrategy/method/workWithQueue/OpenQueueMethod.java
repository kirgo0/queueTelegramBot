package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class OpenQueueMethod implements StrategyMethod {

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        String queueState = MongoDB.getFieldValue(MongoDB.QUEUE_STATE, chatId);

        if(queueState.equalsIgnoreCase(QueueState.CLOSED.toString())) {
            MongoDB.updateField(MongoDB.QUEUE_STATE, QueueState.IN_PROCESS.toString(),chatId);
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.QUEUE_IS_OPENED, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                            .get()
            );
        } else {
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.ERROR_CAUSE_QUEUE_IS_OPENED, TextFormat.Monocular)
                            .addTextLine()
                            .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                            .get()
            );
        }
        response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
        return List.of(response);
    }
}