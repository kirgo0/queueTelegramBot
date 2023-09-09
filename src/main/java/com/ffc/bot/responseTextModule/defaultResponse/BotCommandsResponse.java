package main.java.com.ffc.bot.responseTextModule.defaultResponse;

public class BotCommandsResponse {

    // TEXT COMMANDS:
//        createqueue - створити нову чергу
//        getqueue - отримати чергу
//        closequeue - закрити чергу
//        openqueue - відкрити чергу
//        setdqs - (число) задати розмір черги за замовчуванням
//        startcallqueue - почати прохід по черзі
//        stopcallqueue - зупинити прохід по черзі
//        skipqueuemember - пропустити першого користувача в черзі (якщо заснув)
//        getinfo - як користуватись ботом

    public static final String CREATE_QUEUE = "/createQueue - створити нову чергу";
    public static final String GET_QUEUE = "/getQueue - отримати чергу";
    public static final String CLOSE_QUEUE = "/closeQueue - закрити чергу";
    public static final String OPEN_QUEUE = "/openQueue - відкрити чергу";
    public static final String SET_DQS = "/setdqs - [число] задати розмір черги за замовчуванням";
    public static final String START_CALL_QUEUE = "/startCallQueue - почати прохід по черзі";
    public static final String STOP_CALL_QUEUE = "/stopCallQueue - зупинити прохід по черзі";
    public static final String SKIP_QUEUE_MEMBER = "/skipQueueMember - пропустити першого користувача в черзі (якщо заснув)";
    public static final String GET_INFO = "/getInfo - як користуватись ботом";
}
