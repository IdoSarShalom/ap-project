package graph;

import configs.Agent;

/**
 * An agent that determines the maximum of two numeric values.
 * <p>
 * This agent subscribes to two input topics, determines the maximum of the received values,
 * and publishes the result to an output topic. It implements the Agent interface
 * and provides maximum operation as part of the pub/sub system.
 * <p>
 * The agent requires two input topics and can publish to one output topic.
 */
public class MaxAgent implements Agent {

    /** The topics this agent subscribes to */
    private final String[] subscribedTopics;
    
    /** The topics this agent publishes to */
    private final String[] publishedTopics;
    
    /** Reference to the topic manager singleton */
    private final TopicManagerSingleton.TopicManager topicManager;
    
    /** The first operand for maximum operation */
    private Double firstOperand;
    
    /** The second operand for maximum operation */
    private Double secondOperand;
    
    /** The result of the maximum operation */
    private Double result;

    /**
     * Creates a new maximum agent with the specified subscribed and published topics.
     *
     * @param subscribedTopics Array of topics to subscribe to (expecting two topics)
     * @param publishedTopics Array of topics to publish to (expecting one topic)
     */
    public MaxAgent(String[] subscribedTopics, String[] publishedTopics) {
        this.subscribedTopics = subscribedTopics;
        this.publishedTopics = publishedTopics;
        this.topicManager = TopicManagerSingleton.get();

        initializeSubscriptions();
        initializePublications();
    }

    /**
     * Initializes subscriptions to input topics.
     * <p>
     * Subscribes to the first and second topics in the subscribedTopics array.
     */
    private void initializeSubscriptions() {
        if (subscribedTopics.length > 0) {
            topicManager.getTopic(subscribedTopics[0]).subscribe(this);
        }

        if (subscribedTopics.length > 1) {
            topicManager.getTopic(subscribedTopics[1]).subscribe(this);
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
     * Resets the operands to their initial values.
     */
    private void resetOperands() {
        this.firstOperand = 0.0;
        this.secondOperand = 0.0;
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
     * Resets the operands to their initial values.
     */
    @Override
    public void reset() {
        resetOperands();
    }

    /**
     * Processes a message received from a subscribed topic.
     * <p>
     * Updates the appropriate operand based on the topic, and if both
     * operands are set, publishes the result of finding their maximum.
     *
     * @param topic The topic the message was published to
     * @param msg The message that was published
     */
    @Override
    public void callback(String topic, Message msg) {
        double messageValue = msg.asDouble;

        if (Double.isNaN(messageValue)) {
            return;
        }

        updateOperand(topic, messageValue);

        if (areBothOperandsSet() && hasPublishedTopics()) {
            publishResult();
        }
    }

    /**
     * Updates the appropriate operand based on the topic.
     *
     * @param topic The topic the message was published to
     * @param messageValue The numeric value from the message
     */
    private void updateOperand(String topic, double messageValue) {
        if (subscribedTopics.length > 0 && topic.equals(subscribedTopics[0])) {
            firstOperand = messageValue;
        } else if (subscribedTopics.length > 1 && topic.equals(subscribedTopics[1])) {
            secondOperand = messageValue;
        }
    }

    /**
     * Checks if both operands have been set.
     *
     * @return true if both operands are non-null, false otherwise
     */
    private boolean areBothOperandsSet() {
        return firstOperand != null && secondOperand != null;
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
     * Determines the maximum of the operands and publishes it to the output topic.
     */
    private void publishResult() {
        result = Math.max(firstOperand, secondOperand); // Maximum operation
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
        if (subscribedTopics.length > 1) {
            topicManager.getTopic(subscribedTopics[1]).unsubscribe(this);
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
     * Gets the current value of the first operand.
     *
     * @return The value of the first operand
     */
    public Double getFirstOperand() {
        return firstOperand;
    }

    /**
     * Gets the current value of the second operand.
     *
     * @return The value of the second operand
     */
    public Double getSecondOperand() {
        return secondOperand;
    }

    /**
     * Gets the current result of the maximum operation.
     *
     * @return The result of finding the maximum of the operands
     */
    public Double getResult() {
        return result;
    }

    /**
     * Sets the result of the maximum operation.
     *
     * @param result The new result value
     */
    public void setResult(Double result) {
        this.result = result;
    }
} 