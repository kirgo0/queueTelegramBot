package main.java.com.ffc.bot.markupConstructor;

import main.java.com.ffc.bot.responseTextModule.ButtonsText;
import main.java.com.ffc.bot.scheduler.ScheduledTask;
import main.java.com.ffc.bot.scheduler.TaskScheduler;
import main.java.com.ffc.bot.scheduler.WeekNumber;
import main.java.com.ffc.bot.strategy.SchedulerCallbackData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduledTaskMarkupConstructor {


    public static InlineKeyboardMarkup getAllTasksMarkup(List<ScheduledTask> tasks) {

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (ScheduledTask task :
                tasks) {
            var button = new InlineKeyboardButton();
            StringBuilder sb = new StringBuilder();
            sb.append(task.getName()).append(" - ")
                    .append(task.getTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append(" | ")
                            .append(getDayOfWeekValue(task.getDayOfWeek())).append(" | ")
                                .append(getDateValue(TaskScheduler.getScheduledTaskTime(task)));
            button.setText(sb.toString());
            sb = new StringBuilder();
            button.setCallbackData(sb.append(SchedulerCallbackData.SchedulerGet).append(task.getName()).toString());
            keyboard.add(List.of(button));
        }

        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup getSingleTaskMarkup(ScheduledTask task) {

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // menu row

        var backButton = new InlineKeyboardButton();
        backButton.setText(ButtonsText.BACK_IN_MENU);
        backButton.setCallbackData(SchedulerCallbackData.SchedulerMenu.toString());

        var deleteButton = new InlineKeyboardButton();
        deleteButton.setText(ButtonsText.DENY_ACTION);

        StringBuilder sb = new StringBuilder();
        sb.append(SchedulerCallbackData.SchedulerTaskDelete).append(task.getName());

        deleteButton.setCallbackData(sb.toString());

        keyboard.add(List.of(backButton, deleteButton));

        // minutes row

        var minutesRow = getControlButtons(SchedulerCallbackData.SchedulerMinusMin, task.getName());

        var minutesCenterButton = new InlineKeyboardButton();
        minutesCenterButton.setText(task.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        minutesCenterButton.setCallbackData(" ");
        keyboard.add(List.of(minutesCenterButton));

        minutesRow.addAll(getControlButtons(SchedulerCallbackData.SchedulerPlusMin, task.getName()));

        keyboard.add(minutesRow);

        // days row
        var daysRow = getControlButtons(SchedulerCallbackData.SchedulerMinusDay, task.getName());

        var daysCenterButton = new InlineKeyboardButton();
        daysCenterButton.setText(getDayOfWeekValue(task.getDayOfWeek()));
        daysCenterButton.setCallbackData(" ");
        daysRow.add(daysCenterButton);

        daysRow.addAll(getControlButtons(SchedulerCallbackData.SchedulerPlusDay, task.getName()));

        keyboard.add(daysRow);

        // week row
        LocalDate currentDate = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeekOfYear = currentDate.get(weekFields.weekOfWeekBasedYear());

        var weekRow = getControlButtons(SchedulerCallbackData.SchedulerFirstWeek, task.getName());
        sb = new StringBuilder();
        sb.append(weekRow.get(0).getText());
        if(currentWeekOfYear % 2 != 0) {
            sb.append("(поточний)");
        }
        if(task.getWeekNumber() == WeekNumber.FIRST) {
            sb.append(" ✅");
        }
        weekRow.get(0).setText(sb.toString());
        sb = new StringBuilder();
        var weekRow2 = getControlButtons(SchedulerCallbackData.SchedulerSecondWeek, task.getName());
        sb.append(weekRow2.get(0).getText());
        if(currentWeekOfYear % 2 == 0) {
            sb.append("(поточний)");
        }
        if(task.getWeekNumber() == WeekNumber.SECOND) {
            sb.append(" ✅");
        }
        weekRow2.get(0).setText(sb.toString());
        weekRow.addAll(weekRow2);

        keyboard.add(weekRow);

        // queueSize row

        var sizeRow = getControlButtons(SchedulerCallbackData.SchedulerMinusQueueSize, task.getName());

        sizeRow.addAll(getControlButtons(SchedulerCallbackData.SchedulerPlusQueueSize, task.getName()));

        var sizeTitle = new InlineKeyboardButton("Розмір черги - " + task.getQueueSize());
        sizeTitle.setCallbackData(" ");
        keyboard.add(List.of(sizeTitle));
        keyboard.add(sizeRow);

        return new InlineKeyboardMarkup(keyboard);
    }

    private static List<InlineKeyboardButton> getControlButtons(SchedulerCallbackData buttonCallback, String name) {
        List<String> buttonSymbols = new ArrayList<>();
        List<Integer> buttonData;

        switch (buttonCallback) {
            case SchedulerMinusMin:{
                buttonSymbols = List.of("-60","-15","-5");
                break;
            }
            case SchedulerMinusQueueSize: {
                buttonSymbols = List.of("-10","-5","-1");
                break;
            }
            case SchedulerPlusMin:{
                buttonSymbols = List.of("+5","+15","+60");
                break;
            }
            case SchedulerPlusQueueSize: {
                buttonSymbols = List.of("+1","+5","+10");
                break;
            }
            case SchedulerMinusDay: {
                buttonSymbols = List.of("◀");
                break;
            }
            case SchedulerPlusDay: {
                buttonSymbols = List.of("▶");
                break;
            }
            case SchedulerFirstWeek: {
                buttonSymbols = List.of("1 тиждень");
                break;
            }
            case SchedulerSecondWeek: {
                buttonSymbols = List.of("2 тиждень");
                break;
            }
        }

        if (buttonCallback == SchedulerCallbackData.SchedulerMinusMin) {
            buttonData = List.of(60,15,5);
        } else if (buttonCallback == SchedulerCallbackData.SchedulerPlusMin) {
            buttonData = List.of(5,15,60);
        } else if (buttonCallback == SchedulerCallbackData.SchedulerMinusQueueSize) {
            buttonData = List.of(10,5,1);
        } else if (buttonCallback == SchedulerCallbackData.SchedulerPlusQueueSize) {
            buttonData = List.of(1,5,10);
        } else {
            buttonData = null;
        }

        ArrayList<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < buttonSymbols.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonSymbols.get(i));
            StringBuilder sb = new StringBuilder();
            sb.append(buttonData != null ? buttonData.get(i) : "").append(buttonCallback).append(name);
            button.setCallbackData(sb.toString());
            row.add(button);
        }
        return row;
    }

    private static String getDayOfWeekValue(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
        case MONDAY:
            return "Понеділок";
        case TUESDAY:
            return "Вівторок";
        case WEDNESDAY:
            return "Середа";
        case THURSDAY:
            return "Четвер";
        case FRIDAY:
            return "П'ятниця";
        case SATURDAY:
            return "Субота";
        case SUNDAY:
            return "Неділя";
        default:
            return "Невідомо";
        }
    }

    private static String getDateValue(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd.MM"));
    }
}
