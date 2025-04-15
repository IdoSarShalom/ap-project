package graph;

import configs.Agent;

import java.util.function.BinaryOperator;

public class BinOpAgent implements Agent {

    private final String agentName;
    private final String firstInputTopicName;
    private final String secondInputTopicName;
    private final String outputTopicName;
    private final BinaryOperator<Double> binaryOperator;
    private Double firstArgument;
    private Double secondArgument;

    private final TopicManagerSingleton.TopicManager topicManager;

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

    private void initializeTopics() {
        Topic firstInputtedTopic = topicManager.getTopic(firstInputTopicName);
        Topic secondInputtedTopic = topicManager.getTopic(secondInputTopicName);
        Topic outputTopic = topicManager.getTopic(outputTopicName);

        subscribeToTopics(firstInputtedTopic, secondInputtedTopic);
        registerAsPublisher(outputTopic);
    }

    private void subscribeToTopics(Topic firstInputtedTopic, Topic secondInputtedTopic) {
        firstInputtedTopic.subscribe(this);
        secondInputtedTopic.subscribe(this);
    }

    private void registerAsPublisher(Topic outputTopic) {
        outputTopic.addPublisher(this);
    }

    @Override
    public String getName() {
        return agentName;
    }

    @Override
    public void reset() {
        resetArguments();
    }

    private void resetArguments() {
        firstArgument = 0.0;
        secondArgument = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        if (isMessageValid(msg)) {
            updateArguments(topic, msg.asDouble);
            if (areBothArgumentsSet()) {
                publishResult();
            }
        }
    }

    private boolean isMessageValid(Message msg) {
        return !Double.isNaN(msg.asDouble);
    }

    private void updateArguments(String topic, double value) {
        if (topic.equals(firstInputTopicName)) {
            firstArgument = value;
        } else if (topic.equals(secondInputTopicName)) {
            secondArgument = value;
        }
    }

    private boolean areBothArgumentsSet() {
        return firstArgument != null && secondArgument != null;
    }

    private void publishResult() {
        double result = binaryOperator.apply(firstArgument, secondArgument);
        Topic outputTopic = topicManager.getTopic(outputTopicName);
        outputTopic.publish(new Message(result));
    }

    @Override
    public void close() {
        unsubscribeFromTopics();
        removeAsPublisher();
    }

    private void unsubscribeFromTopics() {
        topicManager.getTopic(firstInputTopicName).unsubscribe(this);
        topicManager.getTopic(secondInputTopicName).unsubscribe(this);
    }

    private void removeAsPublisher() {
        topicManager.getTopic(outputTopicName).removePublisher(this);
    }
}
