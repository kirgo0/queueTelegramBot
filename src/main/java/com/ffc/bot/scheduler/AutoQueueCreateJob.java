package main.java.com.ffc.bot.scheduler;

import main.java.com.ffc.bot.Responder;
import main.java.com.ffc.bot.strategy.AutoCreateQueueMethod;
import main.java.com.ffc.bot.strategy.AutoStrategyMethod;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AutoQueueCreateJob implements Job {

    private String chatId;
    private String name;
    private Responder responder;
    private TaskScheduler scheduler;

    public AutoQueueCreateJob() {}

    public AutoQueueCreateJob(String chatId, String name, Responder responder, TaskScheduler scheduler) {
        this.chatId = chatId;
        this.name = name;
        this.responder = responder;
        this.scheduler = scheduler;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        this.chatId = jobExecutionContext.getMergedJobDataMap().getString("chatId");
        this.name = jobExecutionContext.getMergedJobDataMap().getString("name");
        this.responder = (Responder) jobExecutionContext.getMergedJobDataMap().get("Responder");
        this.scheduler = (TaskScheduler) jobExecutionContext.getMergedJobDataMap().get("TaskScheduler");

        responder.SendMessages(new AutoCreateQueueMethod(scheduler).getResponse(chatId,name));
    }
}
