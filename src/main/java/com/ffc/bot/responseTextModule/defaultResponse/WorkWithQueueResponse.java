package main.java.com.ffc.bot.responseTextModule.defaultResponse;

public class WorkWithQueueResponse {

    // GOOD RESPONSES:

    public static final String QUEUE_IS_CLOSED = "Черга закрита";
    public static final String QUEUE_IS_OPENED = "Черга відкрита";

    public static final String CURRENT_QUEUE_SIZE = "Поточна довжина черги - ";
    public static final String DEFAULT_QUEUE_SIZE_CHANGED = "Встановлено розмір черги за замовчуванням - ";
    public static final String CURRENT_QUEUE_SIZE_CHANGED = "Розмір поточної черги було змінено.";

    // BAD RESPONSES:

    public static final String ERROR_CAUSE_QUEUE_IS_OPENED = "Операція неможлива, спочатку необхідно закрити цю чергу";
    public static final String CANT_SET_QUEUE_SIZE = "Неможливо становити розмір черги менше 2 і більше 100";
    public static final String CANT_CHANGE_QUEUE_SIZE = "Не вдалося змінити розмір поточної чеги";

//    public static final String QUEUE_IS_OPENED = "Черга відкрита";
}
