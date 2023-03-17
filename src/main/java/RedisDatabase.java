import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RedisDatabase {

    private Jedis jedis;

    public RedisDatabase(Jedis db) {
        jedis = db;
    }

    public List<String> getMessages(String groupName, String senderName, String timestamp) {
        String pattern = groupName + "-" + senderName + "-" + timestamp + "*";
        return getMessage(pattern);
    }

    public List<String> getMessages(String groupName, String senderName) {
        String pattern = groupName + "-" + senderName + "-*";
        return getMessage(pattern);
    }

    public List<String> getMessages(String groupName) {
        String pattern = groupName + "-*";
        return getMessage(pattern);
    }

//    private List<String> getMessage(String pattern) {
//        Pipeline pipeline = jedis.pipelined();
//        pipeline.keys(pattern);
//        List<Object> results = pipeline.syncAndReturnAll();
//        List<String> messages = jedis.mget(results.toArray(new String[0]));
//        return messages;
//    }

//    private List<String> getMessage(String pattern) {
//        Pipeline pipeline = jedis.pipelined();
//        pipeline.keys(pattern);
//        List<Object> results = pipeline.syncAndReturnAll();
//        System.out.println("results: " + results);
//
//        List<String> messages = new ArrayList<>();
//        for (Object result : results) {
//            String key = result.toString();
//            String message = jedis.get(key);
//            System.out.println(key + " -> " + message);
//            if (message == null) {
//                System.err.println("No value found for key: " + key);
//            } else {
//                messages.add(message);
//            }
//        }
//        return messages;
//    }

    private List<String> getMessage(String pattern) {
        Pipeline pipeline = jedis.pipelined();
        pipeline.keys(pattern);
        List<Object> results = pipeline.syncAndReturnAll();
//        System.out.println("results: " + results);

        String[] keys = results.stream()
                .map(Object::toString)
                .map(key -> key.substring(1, key.length() - 1))
                .toArray(String[]::new);
//        System.out.println("String Keys: " + Arrays.toString(keys));

        List<String> messages = new ArrayList<>();
        for (String key : keys) {
            String message = jedis.get(key);
            if (message == null) {
                System.err.println("No value found for key: " + key);
            } else {
                messages.add(message);
            }
        }
//        System.out.println(messages + " ((messages");
        return messages;
    }



    public void showKeys(){
        Set<String> keys = jedis.keys("*");
        int index = 0;
        for (String key : keys) {
            System.out.println(": " + key + " messages: " + jedis.get(key));
        }
    }

    public void addMessage(String groupName, String senderName, String message) {
        String timestamp = giveTime();
        String key = groupName + "-" + senderName + "-" + timestamp;
        jedis.set(key, message);
    }

    public void deleteMessage(String groupName, String senderName, String timestamp) {
        String key = groupName + "-" + senderName + "-" + timestamp;
        jedis.del(key);
    }

    private static String giveTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
    }
}
