package main.java.com.ffc.bot.specialMessage;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class AuthoriseMessage extends SendMessage {

    public AuthoriseMessage() {
        setParseMode("HTML");
    }
}
