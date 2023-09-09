package main.java.com.ffc.bot.strategy.textStrategy;

import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.BotCommandsResponse;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.strategy.textStrategy.method.StrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class QueueIsNotOpenMethod implements StrategyMethod {
    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        response.setText(
                new ResponseTextBuilder()
                        .addText(DefaultQueueResponse.QUEUE_IS_NOT_OPENED, TextFormat.Monocular)
                        .addTextLine()
                        .addTextLine(BotCommandsResponse.OPEN_QUEUE, TextFormat.Bold)
                        .addTextLine(BotCommandsResponse.CREATE_QUEUE, TextFormat.Bold)
                        .get()
        );
        return List.of(response);

    }
}
