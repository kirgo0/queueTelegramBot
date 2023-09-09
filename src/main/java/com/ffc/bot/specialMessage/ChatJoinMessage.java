package main.java.com.ffc.bot.specialMessage;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class ChatJoinMessage extends SendMessage {

    public ChatJoinMessage() {
        setParseMode("HTML");
    }
}
