package main.java.com.ffc.bot.responseTextModule.defaultResponse;

public class DefaultQueueResponse {

    // GOOD RESPONSES:

    public static final String CHOOSE_A_PLACE = "Оберіть місце в черзі знизу, натиснувши на будь-яку з кнопок";
    public static final String AUTHORISED_MESSAGE_TITLE = "✅ Ви успішно авторизувались ✅";
    public static final String AUTHORISED_MESSAGE_BODY = "Тепер вам доступні всі функції бота";


    // BAD RESPONSES:
    public static final String NEED_TO_AUTHORISE  = "Для викорситання цього бота необхідно авторизуватись";
    public static final String QUEUE_IS_NOT_OPENED = "Операція неможлива, спочатку необхідно відкрити чергу";
    public static final String QUEUE_DOES_NOT_EXISTS = "Операція неможлива, спочатку необхідно створити чергу";

}
