package main.java.com.ffc.bot.scheduler;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ScheduledTask {
    private String chatId;
    private String name;
    private ActionType action;
    private DayOfWeek dayOfWeek;
    private LocalTime time;
    private WeekNumber weekNumber;
    private int queueSize;

    public ScheduledTask(){}

    public ScheduledTask(String chatId, String name, ActionType action, DayOfWeek dayOfWeek, LocalTime time, WeekNumber weekNumber, int queueSize) {
        this.chatId = chatId;
        this.name = name;
        this.action = action;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.weekNumber = weekNumber;
        this.queueSize = queueSize;
    }

    public String getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public ActionType getAction() {
        return action;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getTime() {
        return time;
    }

    public WeekNumber getWeekNumber() {
        return weekNumber;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setWeekNumber(WeekNumber weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
}

