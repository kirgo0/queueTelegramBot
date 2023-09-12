package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.state.QueueState;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class ChangeQueueViewMethod implements StrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        boolean prevView = MongoDB.getQueueView(chatId);
        MongoDB.setQueueView(chatId,!prevView);

        ResponseTextBuilder rs = new ResponseTextBuilder();
        rs.startFormat(TextFormat.Italic)
                .addText(WorkWithQueueResponse.QUEUE_VIEW_CHANGED)
                .addText(
                        prevView ? WorkWithQueueResponse.QUEUE_VIEW_LIST
                                : WorkWithQueueResponse.QUEUE_VIEW_TABLE, TextFormat.Bold)
                .endFormat(TextFormat.Italic);
        if(!MongoDB.getFieldValue(MongoDB.QUEUE_STATE, chatId).equalsIgnoreCase(QueueState.CLOSED.toString())) {
            rs.addTextLine().addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE);
            response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));
        } else {
            rs
                    .addTextLine()
                    .startFormat(TextFormat.Bold)
                    .addTextLine(BotCommandsResponse.OPEN_QUEUE)
                    .addTextLine(BotCommandsResponse.CREATE_QUEUE)
                    .endFormat(TextFormat.Bold);
        }

        response.setText(rs.get());

        return List.of(response);
    }
}
