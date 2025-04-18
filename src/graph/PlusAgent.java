package graph;

import configs.Agent;

public class PlusAgent implements Agent {

    private final String[] subscribedTopics;
    private final String[] publishedTopics;
    private final TopicManagerSingleton.TopicManager topicManager;
    private Double firstOperand;
    private Double secondOperand;
    private Double result;

    public PlusAgent(String[] subscribedTopics, String[] publishedTopics) {
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

        if (subscribedTopics.length > 1) {
            topicManager.getTopic(subscribedTopics[1]).subscribe(this);
        }
    }

    private void initializePublications() {
        if (publishedTopics.length > 0) {
            topicManager.getTopic(publishedTopics[0]).addPublisher(this);
        }
    }

    private void resetOperands() {
        this.firstOperand = 0.0;
        this.secondOperand = 0.0;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void reset() {
        resetOperands();
    }

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

    private void updateOperand(String topic, double messageValue) {
        if (subscribedTopics.length > 0 && topic.equals(subscribedTopics[0])) {
            firstOperand = messageValue;
        } else if (subscribedTopics.length > 1 && topic.equals(subscribedTopics[1])) {
            secondOperand = messageValue;
        }
    }

    private boolean areBothOperandsSet() {
        return firstOperand != null && secondOperand != null;
    }

    private boolean hasPublishedTopics() {
        return publishedTopics.length > 0;
    }

    private void publishResult() {
        result = firstOperand + secondOperand;
        topicManager.getTopic(publishedTopics[0]).publish(new Message(result));
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
        if (subscribedTopics.length > 1) {
            topicManager.getTopic(subscribedTopics[1]).unsubscribe(this);
        }
    }

    private void removeFromPublishers() {
        if (publishedTopics.length > 0) {
            topicManager.getTopic(publishedTopics[0]).removePublisher(this);
        }
    }

    public String[] getSubscribedTopics() {
        return subscribedTopics;
    }

    public String[] getPublishedTopics() {
        return publishedTopics;
    }

    public TopicManagerSingleton.TopicManager getTopicManager() {
        return topicManager;
    }

    public Double getFirstOperand() {
        return firstOperand;
    }

    public Double getSecondOperand() {
        return secondOperand;
    }

    public Double getResult() {
        return result;
    }

    public void setResult(Double result) {
        this.result = result;
    }
}
