import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                    subscribe(db, groupkey, name);
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
        boolean checked = false;
        String group_name;
        do {
            System.out.print("Group name: ");
            group_name = line_input();
            checked = check_same_groupName(db, group_name);
        }while (checked);
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
        save_message(db, groupkey, message);
    }

    private static void subscribe(Jedis db, String groupkey, String name) {
        MyJedis jedisPubSub = new MyJedis();

        new Thread(() -> {
            System.out.println("Subscribing to channel " + groupkey + " ...");
            add_member(db, groupkey, name);
            show_messages(db, groupkey);
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
                remove_member(db, groupkey, name);

                try {
                    // Sleep for 5 seconds
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void show_messages(Jedis db, String groupkey) {
        Group g = JSON_to_object(db, groupkey);
        assert g != null;
        int index=0;
        for(String message: g.getMessages())
        {
            System.out.println(++index + ") " + message);
        }
    }

    // save message in database
    private static void save_message(Jedis db, String groupkey, String message) {
        Group g = JSON_to_object(db, groupkey);
        assert g != null;
        g.addMessage(message);
        store_JSON_Redis(groupkey, g, db);
    }

    private static void add_member(Jedis db, String groupkey, String name) {
        Group g = JSON_to_object(db, groupkey);
        g.addMember(name);
        store_JSON_Redis(groupkey, g, db);
    }

    private static void remove_member(Jedis db, String groupkey, String name) {
        Group g = JSON_to_object(db, groupkey);
        assert g != null;
        g.removeMember(name);
        store_JSON_Redis(groupkey, g, db);
    }

    // check if the group_name has been used then it return True
    private static boolean check_same_groupName(Jedis db, String group_name) {
        Set<String> keys = db.keys("*");

        for(String key:keys)
        {
            if(group_name.equals(key))
            {
                return true;
            }
        }
        return false;
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
        group.setCreatedTime(giveTime());
        group.setDescription(description);
        return group;
    }

    private static String giveTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        return formattedDateTime;
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
            System.out.print(" (creator: " + g.getCreator() + ", description: " + g.getDescription()+ ", members: ");
            try {
                for (String member : g.getMembers()) {
                    System.out.print(member + ", ");
                }
            }catch (Exception e)
            {

            }
            System.out.println(")");
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
