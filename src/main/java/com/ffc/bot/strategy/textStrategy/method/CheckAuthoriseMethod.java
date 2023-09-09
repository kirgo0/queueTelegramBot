package main.java.com.ffc.bot.strategy.textStrategy.method;

import main.java.com.ffc.bot.PropertiesReader;
import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.responseTextModule.ResponseTextBuilder;
import main.java.com.ffc.bot.responseTextModule.TextFormat;
import main.java.com.ffc.bot.responseTextModule.defaultResponse.DefaultQueueResponse;
import main.java.com.ffc.bot.specialMessage.AuthoriseMessage;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class CheckAuthoriseMethod implements StrategyMethod {

    public List<BotApiMethod> getResponse(Update update, SendMessage response, String chatId) {
        AuthoriseMessage authoriseMessage = new AuthoriseMessage();
        authoriseMessage.setParseMode("HTML");
        authoriseMessage.setText(
                new ResponseTextBuilder()
                        .addText(DefaultQueueResponse.NEED_TO_AUTHORISE, TextFormat.Monocular)
                        .get()
        );
        authoriseMessage.setChatId(chatId);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton redirectButton = new InlineKeyboardButton(ButtonsText.AUTHORISE_BUTTON);

        StringBuilder sb = new StringBuilder();
        sb.append("https://t.me/").append(PropertiesReader.getProperty("bot_name"));
        redirectButton.setUrl(sb.toString());
        keyboard.add(List.of(redirectButton));

        authoriseMessage.setReplyMarkup(new InlineKeyboardMarkup(keyboard));
        return List.of(authoriseMessage);
    }
}
