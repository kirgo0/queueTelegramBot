package main.java.com.ffc.bot.strategy.textStrategy.method.workWithQueue;

import main.java.com.ffc.bot.markupConstructor.SavedQueuesConstructor;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.WorkWithQueueResponse;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class GetSavedQueuesMethod implements TextStrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {

        response.setText(new ResponseTextBuilder()
                    .addText(WorkWithQueueResponse.SAVED_QUEUES_LIST)
                    .get()
        );

        response.setReplyMarkup(SavedQueuesConstructor.getSavedQueuesMarkup(chatId));

        return List.of(response);
    }
}
