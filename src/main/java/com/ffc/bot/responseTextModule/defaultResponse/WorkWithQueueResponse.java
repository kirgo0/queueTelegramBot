package main.java.com.ffc.bot.responseTextModule.defaultResponse;

public class WorkWithQueueResponse {

    // GOOD RESPONSES:

    public static final String QUEUE_IS_CLOSED = "Черга закрита";
    public static final String QUEUE_IS_OPENED = "Черга відкрита";

    public static final String CURRENT_QUEUE_SIZE = "Поточна довжина черги - ";
    public static final String DEFAULT_QUEUE_SIZE_CHANGED = "Встановлено розмір черги за замовчуванням -";
    public static final String CURRENT_QUEUE_SIZE_CHANGED = "Розмір поточної черги було змінено.";
    public static final String QUEUE_VIEW_CHANGED = "Встановлено вигляд черги за замовчуванням -";

    public static final String QUEUE_NORMALIZED = "Всі порожні місця в черзі видалені";
    public static final String QUEUE_VIEW_LIST = "Список";
    public static final String QUEUE_VIEW_TABLE = "Таблиця";

    // BAD RESPONSES:

    public static final String QUEUE_DOES_NOT_HAVE_EMPTY_PLACES = "Операція неможлива, в черзі немає порожніх місць";
    public static final String ERROR_CAUSE_QUEUE_IS_OPENED = "Операція неможлива, спочатку необхідно закрити цю чергу";
    public static final String CANT_SET_QUEUE_SIZE = "Неможливо становити розмір черги менше 2 і більше 100";
    public static final String CANT_CHANGE_QUEUE_SIZE = "Не вдалося змінити розмір поточної черги";
    public static final String QUEUE_IS_EMPTY = "Операція неможлива, черга ще не заповнена";
//    public static final String QUEUE_IS_OPENED = "Черга відкрита";
}
