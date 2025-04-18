package graph;

import configs.Agent;

/**
 * An agent that doubles a numeric value.
 * <p>
 * This agent subscribes to one input topic, multiplies the received value by 2,
 * and publishes the result to an output topic. It implements the Agent interface
 * and provides doubling operation as part of the pub/sub system.
 * <p>
 * The agent requires one input topic and can publish to one output topic.
 */
public class DoubleAgent implements Agent {

    /** The topics this agent subscribes to */
    private final String[] subscribedTopics;
    
    /** The topics this agent publishes to */
    private final String[] publishedTopics;
    
    /** Reference to the topic manager singleton */
    private final TopicManagerSingleton.TopicManager topicManager;
    
    /** The operand for the doubling operation */
    private Double operand;
    
    /** The result of the doubling operation */
    private Double result;

    /**
     * Creates a new doubling agent with the specified subscribed and published topics.
     *
     * @param subscribedTopics Array of topics to subscribe to (expecting one topic)
     * @param publishedTopics Array of topics to publish to (expecting one topic)
     */
    public DoubleAgent(String[] subscribedTopics, String[] publishedTopics) {
        this.subscribedTopics = subscribedTopics;
        this.publishedTopics = publishedTopics;
        this.topicManager = TopicManagerSingleton.get();

        initializeSubscriptions();
        initializePublications();
    }

    /**
     * Initializes subscriptions to input topics.
     * <p>
     * Subscribes to the first topic in the subscribedTopics array.
     */
    private void initializeSubscriptions() {
        if (subscribedTopics.length > 0) {
            topicManager.getTopic(subscribedTopics[0]).subscribe(this);
        }
    }

    /**
     * Initializes publications to output topics.
     * <p>
     * Registers as a publisher to the first topic in the publishedTopics array.
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
     * Processes a message received from a subscribed topic.
     * <p>
     * Updates the operand based on the received message, and if it has
     * a topic to publish to, doubles the value and publishes the result.
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
            publishDoubledValue(operand);
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
     * Doubles the input value and publishes it to the output topic.
     *
     * @param originalValue The value to double
     */
    private void publishDoubledValue(double originalValue) {
        result = originalValue * 2.0; // Double the value
        topicManager.getTopic(publishedTopics[0]).publish(new Message(result));
    }

    /**
     * Cleans up this agent's resources.
     * <p>
     * Unsubscribes from input topics and removes itself as a publisher
     * from output topics.
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
     * Gets the list of topics this agent subscribes to.
     *
     * @return Array of subscribed topic names
     */
    public String[] getSubscribedTopics() {
        return subscribedTopics;
    }

    /**
     * Gets the list of topics this agent publishes to.
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
     * Gets the current operand value.
     *
     * @return The current operand value
     */
    public Double getOperand() {
        return operand;
    }

    /**
     * Gets the current result of the doubling operation.
     *
     * @return The result of doubling the operand
     */
    public Double getResult() {
        return result;
    }

    /**
     * Sets the result of the doubling operation.
     *
     * @param result The new result value
     */
    public void setResult(Double result) {
        this.result = result;
    }
} 