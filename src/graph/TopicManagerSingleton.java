package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton class that provides access to the TopicManager.
 * <p>
 * This class follows the singleton pattern to ensure there is only one
 * instance of the TopicManager throughout the application. It provides
 * a static method to access the singleton instance.
 */
public class TopicManagerSingleton {

    /**
     * Private constructor to prevent instantiation.
     */
    private TopicManagerSingleton() {
    }

    /**
     * Gets the singleton instance of the TopicManager.
     *
     * @return The singleton TopicManager instance
     */
    public static TopicManager get() {
        return TopicManager.INSTANCE;
    }

    /**
     * Inner class that manages topics in the pub/sub system.
     * <p>
     * The TopicManager maintains a thread-safe map of topic names to Topic objects.
     * It provides methods to get or create topics, retrieve all topics, and clear
     * the topic registry.
     */
    public static class TopicManager {

        /** The singleton instance of the TopicManager */
        private static final TopicManager INSTANCE = new TopicManager();

        /** Thread-safe map from topic names to Topic objects */
        private final ConcurrentHashMap<String, Topic> nameToTopic = new ConcurrentHashMap<>();

        /**
         * Private constructor to prevent direct instantiation.
         */
        private TopicManager() {
        }

        /**
         * Gets a topic by name, creating it if it doesn't exist.
         * <p>
         * This method is thread-safe and ensures that each topic name
         * maps to exactly one Topic object.
         *
         * @param name The name of the topic to get or create
         * @return The Topic object for the given name
         */
        public Topic getTopic(String name) {
            return nameToTopic.computeIfAbsent(name, Topic::new);
        }

        /**
         * Gets all topics currently registered in the system.
         *
         * @return A collection of all Topic objects
         */
        public Collection<Topic> getTopics() {
            return nameToTopic.values();
        }

        /**
         * Clears all topics from the registry.
         * <p>
         * This method removes all topics, which can be useful when
         * resetting the system or loading a new configuration.
         */
        public void clear() {
            nameToTopic.clear();
        }
    }
}
