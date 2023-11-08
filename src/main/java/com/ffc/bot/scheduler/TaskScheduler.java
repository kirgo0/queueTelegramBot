package main.java.com.ffc.bot.scheduler;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import main.java.com.ffc.bot.Responder;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

public class TaskScheduler {
    private Responder responder;
    private Scheduler scheduler;

    public TaskScheduler(Responder responder) {
        this.responder = responder;
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public void scheduleTasks(List<ScheduledTask> tasks) {
        for (ScheduledTask task : tasks) {

            LocalDateTime taskDateTime = getScheduledTaskTime(task);

            Trigger trigger;
            JobDetail job;
            String taskId = task.getChatId()+task.getName();
            if (task.getAction() == ActionType.CREATE) {
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(taskId, "createTaskGroup")
                        .startAt(Date.from(taskDateTime.atZone(ZoneId.of("Europe/Kiev")).toInstant()))
                        .build();

                // Створюємо JobDetail і встановлюємо JobDataMap
                job = JobBuilder.newJob(AutoQueueCreateJob.class)
                        .withIdentity(taskId, "createTaskGroup")
                        .build();

                // Встановлюємо параметри через JobDataMap
                job.getJobDataMap().put("chatId", task.getChatId());
                job.getJobDataMap().put("name", task.getName());
                job.getJobDataMap().put("Responder",responder);
                job.getJobDataMap().put("TaskScheduler", this);

                try {
                    scheduler.scheduleJob(job, trigger);
//                    System.out.println("TASK SCHEDULED: " + taskId);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }

            } else if (task.getAction() == ActionType.UPDATE) {
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(taskId, "updateTaskGroup")
                        .startAt(Date.from(taskDateTime.atZone(ZoneId.of("Europe/Kiev")).toInstant()))
                        .build();
                job = JobBuilder.newJob(AutoQueueCreateJob.class)
                        .withIdentity(taskId, "updateTaskGroup")
                        .build();
            } else {
                // Додайте інші умови, якщо потрібно для інших типів дій
                trigger = null;
            }
        }
        try {
            var count = 0;
            for (String groupName : scheduler.getJobGroupNames()) {
                count += scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).toArray().length;
            }
            System.out.println("TOTAL SCHEDULED TASKS NOW: " + count);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void unScheduleTask(String chatId, String taskName) {
        try {
            var taskId = chatId + taskName;
            TriggerKey triggerKey = new TriggerKey(taskId, "createTaskGroup");
            JobKey jobKey = new JobKey(taskId, "createTaskGroup");

            scheduler.unscheduleJob(triggerKey); // Видалення тригера
            scheduler.deleteJob(jobKey); // Видалення завдання

            var count = 0;
            for (String groupName : scheduler.getJobGroupNames()) {
                count += scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).toArray().length;
            }
            System.out.println("TOTAL SCHEDULED TASKS NOW: " + count);

        } catch (Exception e) {
            // Обробка помилки, якщо така є
            e.printStackTrace();
        }
    }

    public void rescheduleTask(ScheduledTask task) {
        var taskId = task.getChatId() + task.getName();
        try {
            var taskDateTime = getScheduledTaskTime(task);
            TriggerKey triggerKey = new TriggerKey(taskId, "createTaskGroup");
            JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(taskId, "createTaskGroup"));

            if (jobDetail != null) {
                // Створюємо новий тригер з оновленим часом виконання (в цьому прикладі використовується CronSchedule)
                Trigger newTrigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerKey)
                        .startAt(Date.from(taskDateTime.atZone(ZoneId.of("Europe/Kiev")).toInstant()))
                        .forJob(jobDetail)
                        .build();

                // Оновлюємо тригер в планувальнику
                scheduler.rescheduleJob(triggerKey, newTrigger);
            }
        } catch (Exception e) {
            // Обробка помилки, якщо така є
            e.printStackTrace();
        }
    }

    public static String getRemainingTime(ScheduledTask task) {
        var time = getScheduledTaskTime(task);
        LocalDateTime currentDateTime = LocalDateTime.now();
        Duration duration = Duration.between(currentDateTime, time);

        long weeks = duration.toDays() / 7;
        long days = duration.toDays() % 7;
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        String weeksStr = getWordForNumber("тиждень", "тижні", "тижнів", weeks);
        String daysStr = getWordForNumber("день", "дні", "днів", days);
        String hoursStr = getWordForNumber("годину", "години", "годин", hours);
        String minutesStr = getWordForNumber("хвилину", "хвилини", "хвилин", minutes);

        StringBuilder result = new StringBuilder();
        int countOfData = 2;
        if (weeks > 0) {
            result.append(weeks).append(" ").append(weeksStr);
            countOfData--;
        }
        if (days > 0) {
            if(countOfData == 1) {
                result.append(", ");
            }
            result.append(days).append(" ").append(daysStr);
            countOfData--;
        }
        if (hours > 0 && countOfData > 0) {
            if(countOfData == 1) {
                result.append(", ");
            }
            result.append(hours).append(" ").append(hoursStr);
            countOfData--;
        }
        if (minutes > 0 && countOfData > 0) {
            if(countOfData == 1) {
                result.append(", ");
            }
            result.append(minutes).append(" ").append(minutesStr);
            countOfData--;
        }
        if(countOfData == 2) result.append("хвилину");

        return result.toString();
    }

    private static String getWordForNumber(String singular, String few, String many, long number) {
        long lastNumber = number % 10;
        if(number > 10 && number < 15) {
            return many;
        }
        if (lastNumber == 1) {
            return singular;
        } else if (lastNumber >= 2 && lastNumber <= 4) {
            return few;
        } else {
            return many;
        }
    }

    public static LocalDateTime getScheduledTaskTime(ScheduledTask task) {
        LocalTime taskTime = task.getTime().withSecond(0);
        LocalDate taskDate = LocalDate.now();

        // Визначаємо день тижня та тиждень з завдання
        DayOfWeek taskDayOfWeek = task.getDayOfWeek();
        WeekNumber taskWeekNumber = task.getWeekNumber();

        // Перевіряємо, чи поточний день є тією ж самою датою, як в `taskDate`, і `taskWeekNumber` дорівнює `WeekNumber.SECOND`

        // Знаходимо ближчий день тижня для запуску завдання
        while (taskDate.getDayOfWeek() != taskDayOfWeek) {
            taskDate = taskDate.plusDays(1);
        }

        LocalDate currentDate = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeekOfYear = currentDate.get(weekFields.weekOfWeekBasedYear());

        if (currentWeekOfYear % 2 != 0 && taskWeekNumber == WeekNumber.SECOND
         || currentWeekOfYear % 2 == 0 && taskWeekNumber == WeekNumber.FIRST) {
            taskDate = taskDate.plusWeeks(1);
        } else if(LocalDateTime.of(taskDate, taskTime).isBefore(LocalDateTime.now())) {
            taskDate = taskDate.plusWeeks(2);
        }

        return LocalDateTime.of(taskDate, taskTime);
    }

    public void shutdownScheduler() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
