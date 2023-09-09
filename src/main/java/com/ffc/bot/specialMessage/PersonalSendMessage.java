package main.java.com.ffc.bot.specialMessage;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class PersonalSendMessage extends SendMessage {

    public PersonalSendMessage() {
        setParseMode("HTML");
    }
}
