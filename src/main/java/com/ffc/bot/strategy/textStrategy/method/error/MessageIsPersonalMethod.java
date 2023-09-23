package main.java.com.ffc.bot.strategy.textStrategy.method.error;

import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.specialMessage.PersonalSendMessage;
import main.java.com.ffc.bot.strategy.textStrategy.method.TextStrategyMethod;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class MessageIsPersonalMethod implements TextStrategyMethod {

    private String userId;

    public MessageIsPersonalMethod(String userId) {
        this.userId = userId;
    }

    @Override
    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        PersonalSendMessage personalSendMessage = new PersonalSendMessage();
        personalSendMessage.setChatId(userId);

        personalSendMessage.setText(
                new ResponseTextBuilder()
                        .addText(DefaultQueueResponse.CANT_USE_THIS_COMMAND_HERE, TextFormat.Monocular)
                        .get()
        );
        return List.of(personalSendMessage);
    }
}
