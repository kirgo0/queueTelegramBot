package main.java.com.ffc.bot.queueHandler;

import main.java.com.ffc.bot.MongoDB;
import org.json.JSONArray;

public class QueueCallModule {

    public static boolean queueEmpty(JSONArray queue) {
        for (int i = 0; i < queue.length(); i++) {
            if(!queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) return false;
        }
        return true;
    }

    public static int getCountOfQueueUsers(JSONArray queue) {
        int count = 0;
        if(!queueEmpty(queue)) {
            for (int i = 0; i < queue.length(); i++) {
                if (!queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) count++;
            }
        }
        return count;
    }

    public static String getFirstQueueUser(JSONArray queue) {
        if(!queueEmpty(queue)) {
            String firstUserId;
            for (int i = 0; i < queue.length(); i++) {
                if(!queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) {
                    firstUserId = queue.getString(i);
                    return firstUserId;
                }
            }
        }
        return MongoDB.EMPTY_QUEUE_MEMBER;
    }

    public static int getFirstQueueUserIndex(JSONArray queue) {
        if(!queueEmpty(queue)) {
            int firstUserIndex;
            for (int i = 0; i < queue.length(); i++) {
                if(!queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) {
                    firstUserIndex = i;
                    return firstUserIndex;
                }
            }
        }
        return -1;
    }

    public static String getNextQueueUser(JSONArray queue) {
        if(getCountOfQueueUsers(queue) > 1) {
            String firstUserIndex;
            for (int i = getFirstQueueUserIndex(queue)+1; i < queue.length(); i++) {
                if(!queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) {
                    firstUserIndex = queue.getString(i);
                    return firstUserIndex;
                }
            }
        }
        return MongoDB.EMPTY_QUEUE_MEMBER;
    }
}
