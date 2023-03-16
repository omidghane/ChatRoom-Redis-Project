import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        // write your code here
        try{
            Scanner input = new Scanner(System.in);
            Jedis db = new Jedis("localhost");
            System.out.println("connection ok");
            System.out.println();

            System.out.println("tell your name: ");
            String name = input.next();


            while(true) {
                Set<String> keys = list_groups(db);
                int index_size = keys.size();
                System.out.println((index_size+1) +": create new group\n"+ (index_size+2) + ": leave");
                String groupkey = choose_groups(db, keys);

                int choice1;
                if(groupkey.equals("create"))
                {
                    choice1 = 3;
                }
                else if(groupkey.equals("quit"))
                {
                    choice1 = 4;
                }
                else {
                    System.out.println("1: write message group \n2: read message group");
                    choice1 = input.nextInt();
                }

                if (choice1 == 1) {
                    publish_message(db, name, groupkey);
                } else if (choice1 == 2) {
                    subscribe(db, groupkey);
                }
                else if(choice1 == 3)
                {
                    create_group(db, name);
                }
                else if(choice1 == 4)
                {
                    break;
                }
                System.out.println();
            }


        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void create_group(Jedis db, String name) {
        System.out.print("Group name: ");
        String group_name = line_input();
        System.out.print("choose a description for group: ");
        String description = line_input();

        Group g;
        g = make_group(name, description);
        store_JSON_Redis(group_name, g, db);
    }

    private static void publish_message(Jedis db, String name, String groupkey) {
        System.out.println("joined " + groupkey);
        System.out.print("write your message : ");

        Scanner input = new Scanner(System.in);
        String channel = groupkey;
        String message = line_input();
        db.publish(channel,name + ": " + message);
    }

    private static void subscribe(Jedis db, String groupkey) {

        MyJedis jedisPubSub = new MyJedis();

        new Thread(() -> {
            System.out.println("Subscribing to channel " + groupkey + " ...");
            db.subscribe(jedisPubSub, groupkey);
            System.out.println("Subscription ended.");
            System.out.println();
        }).start();

        Scanner scanner = new Scanner(System.in);
        boolean keepSubscribing = true;
        while (keepSubscribing) {
            System.out.println("Press Q to unsubscribe.");
            String input = scanner.nextLine();
            if ("Q".equalsIgnoreCase(input)) {
                keepSubscribing = false;
                jedisPubSub.unsubscribe(groupkey);

                try {
                    // Sleep for 5 seconds
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String line_input()
    {
        Scanner input = new Scanner(System.in);
        String tex;
        do {
            tex = input.nextLine();
        }while (tex == null);
        return tex;
    }

    // Convert object to JSON string and store in Redis
    private static void store_JSON_Redis(String groupName,Group group, Jedis db){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonValue = objectMapper.writeValueAsString(group);
            String key = groupName;
            db.set(key, jsonValue);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Group JSON_to_object(Jedis db, String key){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Retrieve JSON string value from Redis and convert back to object
            String retrievedJsonValue = db.get(key);
            Group retrievedGroup = objectMapper.readValue(retrievedJsonValue, Group.class);
            return retrievedGroup;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static Group make_group(String creator, String description){
        // Create object for group with features
        Group group = new Group();
        group.setCreator(creator);
        group.setCreatedTime("2020");
        group.setDescription(description);
//        List<String> members = new ArrayList<>();
//        members.add("Alice");
//        members.add("Bob");
//        members.add("Charlie");
//        group.setMembers(members);
//        List<String> messages = new ArrayList<>();
//        messages.add("hi mamad");
//        messages.add("omid hi");
//        group.setMessages(messages);
        return group;
    }

    private static Set<String> list_groups(Jedis db) {
        Scanner input = new Scanner(System.in);
        // Get all keys in Redis database
        Set<String> keys = db.keys("*");

        // Print out all keys
        int index = 0;
        ArrayList<Integer> keyNumbers = new ArrayList<>();
        for (String key : keys) {
            Group g = JSON_to_object(db, key);
            System.out.print(++index + ": " + key);
            System.out.println(" (creator: " + g.getCreator() + ", description: " + g.getDescription()+ ")");
        }
        return keys;
    }

    private static String choose_groups(Jedis db, Set<String> keys) {
        Scanner input = new Scanner(System.in);

        System.out.print("choose a group: ");
        int choice2 = input.nextInt();

        String groupkey = null;
        int index = 0;
        for (String key : keys) {
            if(++index == choice2)
            {
                groupkey = key;
            }
        }
        if(choice2 == index+1)
        {
            return "create";
        }
        if(choice2 == index+2)
        {
            return "quit";
        }
        return groupkey;
    }

}
