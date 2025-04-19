package graph;

import configs.Agent;

/**
 * An agent that increments numeric values by 1.
 * <p>
 * This agent subscribes to a topic, listens for numeric messages, adds 1 to the
 * received value, and publishes the result to another topic. It implements the
 * Agent interface and provides the increment operation as part of the pub/sub system.
 * <p>
 * The agent requires one input topic and can publish to one output topic.
 */
public class IncAgent implements Agent {

    /** The topics this agent subscribes to */
    private final String[] subscribedTopics;
    
    /** The topics this agent publishes to */
    private final String[] publishedTopics;
    
    /** Reference to the topic manager singleton */
    private final TopicManagerSingleton.TopicManager topicManager;
    
    /** The last received operand value */
    private Double operand;
    
    /** The last calculated result */
    private Double result;

    /**
     * Creates a new increment agent with the specified subscribed and published topics.
     *
     * @param subscribedTopics The topics to subscribe to
     * @param publishedTopics The topics to publish to
     */
    public IncAgent(String[] subscribedTopics, String[] publishedTopics) {
        this.subscribedTopics = subscribedTopics;
        this.publishedTopics = publishedTopics;
        this.topicManager = TopicManagerSingleton.get();

        initializeSubscriptions();
        initializePublications();
    }

    /**
     * Initializes subscriptions to the input topics.
     */
    private void initializeSubscriptions() {
        if (subscribedTopics.length > 0) {
            topicManager.getTopic(subscribedTopics[0]).subscribe(this);
        }
    }

    /**
     * Initializes publications to the output topics.
     */
    private void initializePublications() {
        if (publishedTopics.length > 0) {
            topicManager.getTopic(publishedTopics[0]).addPublisher(this);
        }
    }

    /**
     * Gets the name of this agent.
     *
     * @return The simple class name of this agent
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Resets the agent's state.
     * <p>
     * This implementation has no state to reset.
     */
    @Override
    public void reset() {
        // No state to reset in this agent
    }

    /**
     * Handles messages received from subscribed topics.
     * <p>
     * When a numeric message is received, the agent adds 1 to the value
     * and publishes the result to its output topic.
     *
     * @param topic The topic the message was published to
     * @param msg The message that was published
     */
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

    /**
     * Checks if this agent has any topics to publish to.
     *
     * @return true if the agent has at least one topic to publish to
     */
    private boolean hasPublishedTopics() {
        return publishedTopics.length > 0;
    }

    /**
     * Publishes the incremented value to the output topic.
     *
     * @param originalValue The original value to increment
     */
    private void publishIncrementedValue(double originalValue) {
        result = originalValue + 1.0;
        topicManager.getTopic(publishedTopics[0]).publish(new Message(result));
    }

    /**
     * Closes the agent by unsubscribing from topics and removing itself from publishers.
     */
    @Override
    public void close() {
        unsubscribeFromTopics();
        removeFromPublishers();
    }

    /**
     * Unsubscribes this agent from all subscribed topics.
     */
    private void unsubscribeFromTopics() {
        if (subscribedTopics.length > 0) {
            topicManager.getTopic(subscribedTopics[0]).unsubscribe(this);
        }
    }

    /**
     * Removes this agent from all publishers lists.
     */
    private void removeFromPublishers() {
        if (publishedTopics.length > 0) {
            topicManager.getTopic(publishedTopics[0]).removePublisher(this);
        }
    }

    /**
     * Gets the topics this agent subscribes to.
     *
     * @return Array of subscribed topic names
     */
    public String[] getSubscribedTopics() {
        return subscribedTopics;
    }

    /**
     * Gets the topics this agent publishes to.
     *
     * @return Array of published topic names
     */
    public String[] getPublishedTopics() {
        return publishedTopics;
    }

    /**
     * Gets the topic manager used by this agent.
     *
     * @return The topic manager instance
     */
    public TopicManagerSingleton.TopicManager getTopicManager() {
        return topicManager;
    }

    /**
     * Gets the last received operand value.
     *
     * @return The operand value or null if none has been received
     */
    public Double getOperand() {
        return operand;
    }

    /**
     * Gets the last calculated result.
     *
     * @return The result value or null if none has been calculated
     */
    public Double getResult() {
        return result;
    }

    /**
     * Sets the result value directly.
     * <p>
     * This is used by the Topic class when this agent is registered as a publisher.
     *
     * @param result The result value to set
     */
    public void setResult(Double result) {
        this.result = result;
    }
}
