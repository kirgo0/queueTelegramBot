package main.java.com.ffc.bot.queueHandler;

import main.java.com.ffc.bot.MongoDB;
import org.json.JSONArray;

public class QueueResizeModule {

    public static boolean changeSize(JSONArray queue, int nextSize) {
        if(nextSize < queue.length()) {
            boolean resize = true;
            for (int i = nextSize; i < queue.length(); i++) {
                if(!queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) resize = false;
            }
            if(resize) {
                while(queue.length() != nextSize) {
                    queue.remove(nextSize);
                }
            } else {
                normalizeQueueToSize(queue, nextSize);
            }
        } else {
            for (int i = queue.length(); i < nextSize; i++) {
                queue.put(i, MongoDB.EMPTY_QUEUE_MEMBER);
            }
        }
        return queue.length() == nextSize;
    }

    private static void normalizeQueueToSize(JSONArray queue, int nextSize) {
        int countOfEmpty = 0;
        for (int i = 0; i < queue.length(); i++) {
            if(queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) countOfEmpty++;
        }
        if(queue.length()-nextSize < countOfEmpty) {
            int lastElement = queue.length()-1;
            while(queue.length() > nextSize) {
                if(queue.getString(lastElement).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) {
                    queue.remove(lastElement);
                }
                lastElement--;
            }
        } else if(queue.length()-nextSize == countOfEmpty){
            for (int i = 0; i < queue.length(); i++) {
                if(queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) {
                    queue.remove(i);
                    i--;
                }
            }
        }
    }

    public static boolean normalizeQueue(JSONArray queue) {
        int finalQueueSize = queue.length(), countOfMembers = 0;
        for (int i = 0; i < queue.length(); i++) {
            if(!queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) {
                countOfMembers++;
            }
        }
        for (int i = 0; i < queue.length(); i++) {
            if(queue.getString(i).equalsIgnoreCase(MongoDB.EMPTY_QUEUE_MEMBER)) {
                if(countOfMembers == 0) break;
                queue.remove(i);
                i--;
            } else {
                countOfMembers--;
            }
        }
        if(queue.length() == finalQueueSize) return false;

        for (int i = queue.length(); i < finalQueueSize; i++) {
            queue.put(MongoDB.EMPTY_QUEUE_MEMBER);
        }
        return true;
    }

}
