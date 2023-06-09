import redis.clients.jedis.JedisPubSub;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyJedis extends JedisPubSub {
    private boolean exit = false;

    @Override
    public void onMessage(String channel, String message) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        System.out.println("[" +formattedDateTime + "] " + message );
    }

    @Override
    public void onPMessage(String s, String s1, String s2) {

    }

    @Override
    public void onSubscribe(String s, int i) {
        System.out.println("listening ...");
    }

    @Override
    public void onUnsubscribe(String s, int i) {

    }

    @Override
    public void onPUnsubscribe(String s, int i) {

    }

    @Override
    public void onPSubscribe(String s, int i) {

    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }
}
