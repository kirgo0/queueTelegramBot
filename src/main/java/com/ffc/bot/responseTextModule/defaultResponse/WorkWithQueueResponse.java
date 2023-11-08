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

    public static final String SAVED_QUEUES_TITLE = "\uD83D\uDDD2 Збережені черги \uD83D\uDDD2";
    public static final String SAVED_QUEUES_LIST = "Список збережених черг";
    public static final String QUEUE_SAVED_AS = "Дану чергу було збережено як - ";
    public static final String GET_SAVED_QUEUE = "Перегляд збереженої черги - ";
    public static final String SAVED_QUEUE_REMOVED = "Збережену чергу було видалено";
    public static final String SAVED_QUEUE_LOADED = "Завантажено збережену чергу - ";
    public static final String RESAVED_QUEUE = "Збережену чергу перезаписано";
    public static final String REMOVE_SAVED_QUEUE_AUTH = "Ви дійсно бажаєте видалити збережену чергу - ";
    public static final String SAVED_QUEUE_NOT_REMOVED = "Операція скасована";
    public static final String SAVED_QUEUES_LIST_IS_EMPTY = "Ви ще не зберігали черги";

    // BAD RESPONSES:

    public static final String QUEUE_DOES_NOT_HAVE_EMPTY_PLACES = "Операція неможлива, в черзі немає порожніх місць";
    public static final String ERROR_CAUSE_QUEUE_IS_OPENED = "Операція неможлива, спочатку необхідно закрити цю чергу";
    public static final String CANT_SET_QUEUE_SIZE = "Неможливо становити розмір черги менше 2 і більше 100";
    public static final String CANT_CHANGE_QUEUE_SIZE = "Не вдалося змінити розмір поточної черги";
    public static final String QUEUE_IS_EMPTY = "Операція неможлива, черга ще не заповнена";
    public static final String QUEUE_SIZE_NO_PARAMS = "Для використання команди вкажіть бажаний розмір черги після команди";
    public static final String SAVED_QUEUE_NO_PARAMS = "Для використання команди вкажіть назву збереженої черги після команди";
    public static final String SAVED_QUEUE_ALREADY_EXISTS = "Збережена черга з таким іменем вже існує";
    public static final String MAX_SAVED_QUEUES_COUNT_REACHED = "Неможливо зберегти більше 5 черг";
    public static final String ERROR_CAUSE_SAVED_QUEUE_DOES_NOT_EXISTS = "Операція неможлива, такої збереженої черги не існує";
    public static final String ERROR_CAUSE_SAVED_QUEUE_WRONG_NAME = "Операція неможлива, це ім'я черги зарезервоване в системі";
//    public static final String QUEUE_IS_OPENED = "Черга відкрита";
}
