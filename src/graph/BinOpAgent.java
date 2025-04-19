package graph;

import configs.Agent;

import java.util.function.BinaryOperator;

/**
 * A base agent implementation for binary operations on two input topics.
 * <p>
 * This agent subscribes to two input topics, applies a binary operation to
 * the values received from these topics, and publishes the result to an output topic.
 * It provides the common functionality for agents like PlusAgent, MultAgent, etc.
 * that perform different binary operations.
 */
public class BinOpAgent implements Agent {

    /** The name of this agent */
    private final String agentName;
    
    /** The name of the first input topic */
    private final String firstInputTopicName;
    
    /** The name of the second input topic */
    private final String secondInputTopicName;
    
    /** The name of the output topic */
    private final String outputTopicName;
    
    /** The binary operation to apply to the input values */
    private final BinaryOperator<Double> binaryOperator;
    
    /** The last value received from the first input topic */
    private Double firstArgument;
    
    /** The last value received from the second input topic */
    private Double secondArgument;

    /** Reference to the topic manager singleton */
    private final TopicManagerSingleton.TopicManager topicManager;

    /**
     * Creates a new binary operation agent.
     *
     * @param agentName The name of this agent
     * @param firstInputTopicName The name of the first input topic
     * @param secondInputTopicName The name of the second input topic
     * @param outputTopicName The name of the output topic
     * @param binaryOperation The binary operation to apply to the input values
     */
    public BinOpAgent(String agentName, String firstInputTopicName, String secondInputTopicName,
                      String outputTopicName, BinaryOperator<Double> binaryOperation) {
        this.agentName = agentName;
        this.firstInputTopicName = firstInputTopicName;
        this.secondInputTopicName = secondInputTopicName;
        this.outputTopicName = outputTopicName;
        this.binaryOperator = binaryOperation;
        this.topicManager = TopicManagerSingleton.get();

        initializeTopics();
    }

    /**
     * Initializes the topics this agent interacts with.
     * <p>
     * Gets references to the input and output topics, subscribes to the input topics,
     * and registers as a publisher to the output topic.
     */
    private void initializeTopics() {
        Topic firstInputtedTopic = topicManager.getTopic(firstInputTopicName);
        Topic secondInputtedTopic = topicManager.getTopic(secondInputTopicName);
        Topic outputTopic = topicManager.getTopic(outputTopicName);

        subscribeToTopics(firstInputtedTopic, secondInputtedTopic);
        registerAsPublisher(outputTopic);
    }

    /**
     * Subscribes this agent to the input topics.
     *
     * @param firstInputtedTopic The first input topic
     * @param secondInputtedTopic The second input topic
     */
    private void subscribeToTopics(Topic firstInputtedTopic, Topic secondInputtedTopic) {
        firstInputtedTopic.subscribe(this);
        secondInputtedTopic.subscribe(this);
    }

    /**
     * Registers this agent as a publisher to the output topic.
     *
     * @param outputTopic The output topic
     */
    private void registerAsPublisher(Topic outputTopic) {
        outputTopic.addPublisher(this);
    }

    /**
     * Gets the name of this agent.
     *
     * @return The agent name
     */
    @Override
    public String getName() {
        return agentName;
    }

    /**
     * Resets this agent's state.
     * <p>
     * Clears the stored argument values.
     */
    @Override
    public void reset() {
        resetArguments();
    }

    /**
     * Resets the stored argument values to zero.
     */
    private void resetArguments() {
        firstArgument = 0.0;
        secondArgument = 0.0;
    }

    /**
     * Processes a message received from a subscribed topic.
     * <p>
     * Updates the corresponding argument based on the topic, and
     * publishes the result if both arguments are now set.
     *
     * @param topic The topic the message was published to
     * @param msg The message that was published
     */
    @Override
    public void callback(String topic, Message msg) {
        if (isMessageValid(msg)) {
            updateArguments(topic, msg.asDouble);
            if (areBothArgumentsSet()) {
                publishResult();
            }
        }
    }

    /**
     * Checks if a message contains a valid numeric value.
     *
     * @param msg The message to check
     * @return true if the message contains a valid number, false otherwise
     */
    private boolean isMessageValid(Message msg) {
        return !Double.isNaN(msg.asDouble);
    }

    /**
     * Updates the appropriate argument based on the topic the message came from.
     *
     * @param topic The topic the message was published to
     * @param value The numeric value from the message
     */
    private void updateArguments(String topic, double value) {
        if (topic.equals(firstInputTopicName)) {
            firstArgument = value;
        } else if (topic.equals(secondInputTopicName)) {
            secondArgument = value;
        }
    }

    /**
     * Checks if both arguments have been set.
     *
     * @return true if both arguments are non-null, false otherwise
     */
    private boolean areBothArgumentsSet() {
        return firstArgument != null && secondArgument != null;
    }

    /**
     * Applies the binary operation to the arguments and publishes the result.
     */
    private void publishResult() {
        double result = binaryOperator.apply(firstArgument, secondArgument);
        Topic outputTopic = topicManager.getTopic(outputTopicName);
        outputTopic.publish(new Message(result));
    }

    /**
     * Cleans up this agent's resources.
     * <p>
     * Unsubscribes from the input topics and removes itself as a publisher
     * from the output topic.
     */
    @Override
    public void close() {
        unsubscribeFromTopics();
        removeAsPublisher();
    }

    /**
     * Unsubscribes this agent from its input topics.
     */
    private void unsubscribeFromTopics() {
        topicManager.getTopic(firstInputTopicName).unsubscribe(this);
        topicManager.getTopic(secondInputTopicName).unsubscribe(this);
    }

    /**
     * Removes this agent as a publisher from its output topic.
     */
    private void removeAsPublisher() {
        topicManager.getTopic(outputTopicName).removePublisher(this);
    }
}
