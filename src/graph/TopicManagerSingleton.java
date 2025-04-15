package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    private TopicManagerSingleton() {
    }

    public static TopicManager get() {
        return TopicManager.INSTANCE;
    }

    public static class TopicManager {

        private static final TopicManager INSTANCE = new TopicManager();

        private final ConcurrentHashMap<String, Topic> nameToTopic = new ConcurrentHashMap<>();

        private TopicManager() {
        }

        public Topic getTopic(String name) {
            return nameToTopic.computeIfAbsent(name, Topic::new);
        }

        public Collection<Topic> getTopics() {
            return nameToTopic.values();
        }

        public void clear() {
            nameToTopic.clear();
        }
    }
}
