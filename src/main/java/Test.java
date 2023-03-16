import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException {
        // Connect to Redis server
        Jedis jedis = new Jedis("localhost", 6379);

        // Create object for group with features
        Group group = new Group();
        group.setCreator("John Doe");
        group.setCreatedTime("2020");
        group.setDescription("A group for Redis users");
//        List<String> members = new ArrayList<>();
//        members.add("Alice");
//        members.add("Bob");
//        members.add("Charlie");
//        group.setMembers(members);
//        List<String> messages = new ArrayList<>();
//        messages.add("hi mamad");
//        messages.add("omid hi");
//        group.setMessages(messages);

        // Convert object to JSON string and store in Redis
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonValue = objectMapper.writeValueAsString(group);
        String key = "group:1";
        jedis.set(key, jsonValue);

        // Retrieve JSON string value from Redis and convert back to object
        String retrievedJsonValue = jedis.get(key);
        Group retrievedGroup = objectMapper.readValue(retrievedJsonValue, Group.class);
        System.out.println("Retrieved group: " + retrievedGroup);

        // Update object with new member and convert back to JSON string
        retrievedGroup.getMembers().add("David");
        String updatedJsonValue = objectMapper.writeValueAsString(retrievedGroup);
        jedis.set(key, updatedJsonValue);

        // Retrieve updated JSON string value from Redis and convert back to object
        String updatedRetrievedJsonValue = jedis.get(key);
        Group updatedRetrievedGroup = objectMapper.readValue(updatedRetrievedJsonValue, Group.class);
        System.out.println("Updated retrieved group: " + updatedRetrievedGroup);

        // Delete Redis key
        jedis.del(key);
        System.out.println("Deleted key: " + key);

        // Close Redis client
        jedis.close();
    }

}
