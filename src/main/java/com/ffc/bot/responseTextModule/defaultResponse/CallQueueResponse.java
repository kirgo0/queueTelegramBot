package main.java.com.ffc.bot.responseTextModule.defaultResponse;

public class CallQueueResponse {

    // GOOD RESPONSES:
    public static final String CALL_QUEUE_TITLE = "\uD83D\uDD14 Прохід по черзі \uD83D\uDD14";
    public static final String DEFAULT_RESPONSE = "Учасники ще можуть змінювати свої місця і виходити з черги";

    public static final String FIRST_QUEUE_MEMBER_BODY = "Ви перший в черзі, інші учасники чекають, коли ви відповісте і залишите чергу";
    public static final String NEXT_QUEUE_MEMBER_BODY = "Ви наступний в черзі, готуйтесь відповідати";

    public static final String CALL_QUEUE_STOPPED = "Прохід по черзі зупинено";
    // QUEUE CHANGED RESPONSES
    public static final String QUEUE_ORDER_CHANGED = "Порядок черги змінено";

    // BAD RESPONSES:
    public static final String ERROR_CALL_QUEUE_IS_NOT_STARTED = "Операція неможлива, прохід по черзі ще не почато";
    public static final String QUEUE_IS_NOT_FULL = "Операція неможлива, черга ще не заповнилась";
    public static final String CALL_QUEUE_IS_RUNNING = "Операція неможлива, спочатку необхідно зупинити прохід по черзі";
    public static final String ERROR_CAUSE_USER_IS_NO_LONGER_FIRST = "Операція неможлива, ви більше не знаходитесь на першому місці в черзі";
    public static final String ERROR_CAUSE_USER_IS_NO_LONGER_IN_QUEUE = "Операція неможлива, ви вже вийшли з черги";

    // FINISH RESPONSE
    public static final String CALL_QUEUE_FINISHED = "Прохід по черзі завершено";
    public static final String CALL_QUEUE_LEFT = "Ви вийшли з черги";
}
