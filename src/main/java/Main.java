import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // write your code here
        try{
            Scanner input = new Scanner(System.in);
            Jedis db = new Jedis("localhost");
            System.out.println("connection ok");
            System.out.println();


            db.lrem("groups", 1000, "gr3");
            //store data in redis lists
//            db.lpush("groups", "gr1");
//            db.lpush("groups", "gr2");
//            db.lpush("groups", "gr3");
            // Get the stored data and print it
            List<String> list = db.lrange("groups", 0, 100);

//            for(int i = 0; i<list.size(); i++) {
//                System.out.println("Stored string in redis:: "+list.get(i));
//            }

            System.out.println("tell your name: ");
            String name = input.next();


            while(true) {
                System.out.println("1: write message group \n2: read message group \n3: leave");
                int choice1 = input.nextInt();

                if (choice1 == 1) {
                    list_groups(list);
                    System.out.println("choose a group: ");
                    int choice2 = input.nextInt();
                    System.out.println("joined " + list.get(choice2 - 1));
                    System.out.println("write your message : ");
                    System.out.println();

                    String channel = list.get(choice2 - 1);
                    String message = input.next();
                    db.publish(channel,name + ": " + message);

                } else if (choice1 == 2) {
                    list_groups(list);
                    System.out.println("choose a group: ");
                    int choice2 = input.nextInt();

                    System.out.println("joined " + list.get(choice2 - 1));

                    MyJedis jedisPubSub = new MyJedis();
                    db.subscribe(jedisPubSub , list.get(choice2 - 1));

                }
                else if(choice1 == 3)
                {
                    break;
                }
            }


        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void make_group(String creator, String description){
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
    }

    private static void list_groups(List<String> list) {
        for(int i = 0; i<list.size(); i++) {
            System.out.println(i+1+") Stored group:: "+list.get(i));
        }
    }

}
