package main.java.com.ffc.bot.strategy;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.List;

public interface Strategy {
    List<BotApiMethod> getResponse(Update update);
}
