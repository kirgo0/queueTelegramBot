package main.java.com.ffc.bot.strategy.callbackStrategy.method;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface CallbackStrategyMethod {

    List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId, String userId, String callbackUpdate);
}
