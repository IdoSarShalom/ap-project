package test;


public class App4 {
    public static void main(String[] args) {
        // Create our config
        GenericConfig config = new GenericConfig();
        // Set the path to our file
        config.setConfFile("/home/ido/VSCodeProjects/ap-course-project-ex4/config_files/simple.conf");

        // Build it
        config.create();

        // We can now publish messages to "A" and "B" topics:
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic("A").publish(new Message(5.0));
        tm.getTopic("B").publish(new Message(3.0));

        // The "PlusAgent" will produce 8.0 on "C",
        // Then "IncAgent" sees 8.0 on "C" and publishes 9.0 on "D".

        // We can read "D" from a separate agent or check logs, etc.

        // Finally, close everything
        config.close();
    }
}

