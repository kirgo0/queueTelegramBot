package main.java.com.ffc.bot;

import main.java.com.ffc.bot.scheduler.TaskScheduler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalTime;

/**
 * Hello world!
 *
 */
public class App 
{

    public static void main(String[] args) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            Responder responder = new Responder();
            api.registerBot(responder);
            MongoDB.connectToDatabase();
            TaskScheduler scheduler = new TaskScheduler(responder);
            responder.registerScheduler(scheduler);
            var tasksFromDB = MongoDB.getScheduledTasks();
            if(tasksFromDB != null) {
                scheduler.scheduleTasks(tasksFromDB);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
