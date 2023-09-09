package main.java.com.ffc.bot.queueHandler;

import org.json.JSONArray;

public class QueueResizeModule {

    public static boolean changeSize(JSONArray queue, int nextSize) {
        if(nextSize < queue.length()) {
            boolean resize = true;
            for (int i = nextSize; i < queue.length(); i++) {
                if(!queue.getString(i).equalsIgnoreCase("_")) resize = false;
            }
            if(resize) {
                while(queue.length() != nextSize) {
                    queue.remove(nextSize);
                }
            } else {
                normalizeQueue(queue, nextSize);
            }
        } else {
            for (int i = queue.length(); i < nextSize; i++) {
                queue.put(i, "_");
            }
        }
        return queue.length() == nextSize;
    }

    private static void normalizeQueue(JSONArray queue, int nextSize) {
        int countOfEmpty = 0;
        for (int i = 0; i < queue.length(); i++) {
            if(queue.getString(i).equalsIgnoreCase("_")) countOfEmpty++;
        }
        if(queue.length()-nextSize < countOfEmpty) {
            int lastElement = queue.length()-1;
            while(queue.length() > nextSize) {
                if(queue.getString(lastElement).equalsIgnoreCase("_")) {
                    queue.remove(lastElement);
                }
                lastElement--;
            }
        } else if(queue.length()-nextSize == countOfEmpty){
            for (int i = 0; i < queue.length(); i++) {
                if(queue.getString(i).equalsIgnoreCase("_")) {
                    queue.remove(i);
                    i--;
                }
            }
        }
    }

}
