package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.MongoDB;
import main.java.com.ffc.bot.queueHandler.QueueCallModule;
import main.java.com.ffc.bot.queueHandler.QueueMarkupConstructor;
import main.java.com.ffc.bot.queueHandler.QueueResizeModule;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.glassfish.grizzly.http.server.util.Enumerator;
import org.json.JSONArray;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class NormalizeQueueMethod implements TextStrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        JSONArray queue = new JSONArray(MongoDB.getQueue(chatId));

        boolean queueHasEmptyPlaces = QueueResizeModule.normalizeQueue(queue);

        if(queueHasEmptyPlaces) {
            MongoDB.updateQueue(queue.toString(), chatId);
            response.setText(
                    new ResponseTextBuilder()
                            .addText(WorkWithQueueResponse.QUEUE_NORMALIZED, TextFormat.Italic)
                            .addTextLine()
                            .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                            .get()
            );
        } else {
            if(QueueCallModule.queueEmpty(queue)) {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(WorkWithQueueResponse.QUEUE_IS_EMPTY, TextFormat.Monocular)
                                .addTextLine()
                                .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                                .get()
                );
            } else {
                response.setText(
                        new ResponseTextBuilder()
                                .addText(WorkWithQueueResponse.QUEUE_DOES_NOT_HAVE_EMPTY_PLACES, TextFormat.Monocular)
                                .addTextLine()
                                .addTextLine(DefaultQueueResponse.CHOOSE_A_PLACE)
                                .get()
                );
            }
        }

        response.setReplyMarkup(QueueMarkupConstructor.getMarkup(chatId));

        return List.of(response);
    }
}
