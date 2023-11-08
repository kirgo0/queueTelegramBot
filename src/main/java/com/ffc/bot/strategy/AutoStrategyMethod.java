package main.java.com.ffc.bot.strategy;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.util.List;

public interface AutoStrategyMethod {

    public List<BotApiMethod> getResponse(String chatId, String name);
}
