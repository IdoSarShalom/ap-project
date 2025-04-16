package graph;

import configs.Agent;

public class IncAgent implements Agent {

    private final String[] subscribedTopics;
    private final String[] publishedTopics;
    private final TopicManagerSingleton.TopicManager topicManager;
    private Double operand;

    public IncAgent(String[] subscribedTopics, String[] publishedTopics) {
        this.subscribedTopics = subscribedTopics;
        this.publishedTopics = publishedTopics;
        this.topicManager = TopicManagerSingleton.get();

        initializeSubscriptions();
        initializePublications();
    }

    private void initializeSubscriptions() {
        if (subscribedTopics.length > 0) {
            topicManager.getTopic(subscribedTopics[0]).subscribe(this);
        }
    }

    private void initializePublications() {
        if (publishedTopics.length > 0) {
            topicManager.getTopic(publishedTopics[0]).addPublisher(this);
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void reset() {
        // No state to reset in this agent
    }

    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }

        operand = msg.asDouble;
        if (hasPublishedTopics()) {
            publishIncrementedValue(operand);
        }
    }

    private boolean hasPublishedTopics() {
        return publishedTopics.length > 0;
    }

    private void publishIncrementedValue(double originalValue) {
        double incrementedValue = originalValue + 1.0;
        topicManager.getTopic(publishedTopics[0]).publish(new Message(incrementedValue));
    }

    @Override
    public void close() {
        unsubscribeFromTopics();
        removeFromPublishers();
    }

    private void unsubscribeFromTopics() {
        if (subscribedTopics.length > 0) {
            topicManager.getTopic(subscribedTopics[0]).unsubscribe(this);
        }
    }

    private void removeFromPublishers() {
        if (publishedTopics.length > 0) {
            topicManager.getTopic(publishedTopics[0]).removePublisher(this);
        }
    }

    // Getters for the fields
    public String[] getSubscribedTopics() {
        return subscribedTopics;
    }

    public String[] getPublishedTopics() {
        return publishedTopics;
    }

    public TopicManagerSingleton.TopicManager getTopicManager() {
        return topicManager;
    }

    public Double getOperand() {
        return operand;
    }
}
