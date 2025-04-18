package graph;

import configs.Agent;
import configs.Node;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Graph extends ArrayList<Node> {

    public boolean hasCycles() {
        return this.stream().anyMatch(Node::hasCycles);
    }

    public void createFromTopics() {
        clearGraph();
        Map<Topic, Node> topicNodeMap = new ConcurrentHashMap<>();
        Map<Agent, Node> agentNodeMap = new ConcurrentHashMap<>();
        processTopics(topicNodeMap, agentNodeMap);
    }

    private void clearGraph() {
        clear();
    }

    private void processTopics(Map<Topic, Node> topicNodeMap, Map<Agent, Node> agentNodeMap) {
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

        for (Topic topic : topicManager.getTopics()) {
            Node topicNode = retrieveOrCreateTopicNode(topic, topicNodeMap);
            connectPublishersToTopic(topic, topicNode, agentNodeMap);
            connectSubscribersToTopic(topic, topicNode, agentNodeMap);
        }
    }

    private Node retrieveOrCreateTopicNode(Topic topic, Map<Topic, Node> topicNodeMap) {
        return topicNodeMap.computeIfAbsent(topic, t -> createNewNode(t.name));
    }

    private Node retrieveOrCreateAgentNode(Agent agent, Map<Agent, Node> agentNodeMap) {
        return agentNodeMap.computeIfAbsent(agent, a -> createNewNode(a.getName()));
    }

    private Node createNewNode(String name) {
        Node newNode = new Node(name);
        this.add(newNode);
        return newNode;
    }

    private void connectSubscribersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent subscriber : topic.getSubscribers()) {
            Node agentNode = retrieveOrCreateAgentNode(subscriber, agentNodeMap);
            topicNode.addEdge(agentNode);
            processSubscriberMessage(subscriber, topic, topicNode);
        }
    }

    private void connectPublishersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent publisher : topic.getPublishers()) {
            Node agentNode = retrieveOrCreateAgentNode(publisher, agentNodeMap);
            agentNode.addEdge(topicNode);
            processPublisherMessage(publisher, topic, topicNode);
        }
    }

    private void processSubscriberMessage(Agent subscriber, Topic topic, Node topicNode) {
        if (subscriber instanceof PlusAgent) {
            processPlusAgentSubscriberMessage((PlusAgent) subscriber, topic, topicNode);
        } else if (subscriber instanceof IncAgent) {
            processIncAgentSubscriberMessage((IncAgent) subscriber, topicNode);
        } else if (subscriber instanceof MultAgent) {
            processMultAgentSubscriberMessage((MultAgent) subscriber, topic, topicNode);
        } else if (subscriber instanceof MinusAgent) {
            processMinusAgentSubscriberMessage((MinusAgent) subscriber, topic, topicNode);
        } else if (subscriber instanceof MaxAgent) {
            processMaxAgentSubscriberMessage((MaxAgent) subscriber, topic, topicNode);
        } else if (subscriber instanceof MinAgent) {
            processMinAgentSubscriberMessage((MinAgent) subscriber, topic, topicNode);
        } else if (subscriber instanceof AvgAgent) {
            processAvgAgentSubscriberMessage((AvgAgent) subscriber, topic, topicNode);
        } else if (subscriber instanceof DecAgent) {
            processDecAgentSubscriberMessage((DecAgent) subscriber, topicNode);
        } else if (subscriber instanceof NegAgent) {
            processNegAgentSubscriberMessage((NegAgent) subscriber, topicNode);
        } else if (subscriber instanceof AbsAgent) {
            processAbsAgentSubscriberMessage((AbsAgent) subscriber, topicNode);
        } else if (subscriber instanceof DoubleAgent) {
            processDoubleAgentSubscriberMessage((DoubleAgent) subscriber, topicNode);
        }
    }

    private void processPlusAgentSubscriberMessage(PlusAgent plusAgent, Topic topic, Node topicNode) {
        Double operand = getPlusAgentOperand(plusAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private Double getPlusAgentOperand(PlusAgent plusAgent, Topic topic) {
        if (topic.name.equals(plusAgent.getSubscribedTopics()[0])) {
            return plusAgent.getFirstOperand();
        }
        return plusAgent.getSecondOperand();
    }

    private void processMultAgentSubscriberMessage(MultAgent multAgent, Topic topic, Node topicNode) {
        Double operand = getMultAgentOperand(multAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private Double getMultAgentOperand(MultAgent multAgent, Topic topic) {
        if (topic.name.equals(multAgent.getSubscribedTopics()[0])) {
            return multAgent.getFirstOperand();
        }
        return multAgent.getSecondOperand();
    }

    private void processMinusAgentSubscriberMessage(MinusAgent minusAgent, Topic topic, Node topicNode) {
        Double operand = getMinusAgentOperand(minusAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private Double getMinusAgentOperand(MinusAgent minusAgent, Topic topic) {
        if (topic.name.equals(minusAgent.getSubscribedTopics()[0])) {
            return minusAgent.getFirstOperand();
        }
        return minusAgent.getSecondOperand();
    }

    private void processMaxAgentSubscriberMessage(MaxAgent maxAgent, Topic topic, Node topicNode) {
        Double operand = getMaxAgentOperand(maxAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private Double getMaxAgentOperand(MaxAgent maxAgent, Topic topic) {
        if (topic.name.equals(maxAgent.getSubscribedTopics()[0])) {
            return maxAgent.getFirstOperand();
        }
        return maxAgent.getSecondOperand();
    }

    private void processMinAgentSubscriberMessage(MinAgent minAgent, Topic topic, Node topicNode) {
        Double operand = getMinAgentOperand(minAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private Double getMinAgentOperand(MinAgent minAgent, Topic topic) {
        if (topic.name.equals(minAgent.getSubscribedTopics()[0])) {
            return minAgent.getFirstOperand();
        }
        return minAgent.getSecondOperand();
    }

    private void processAvgAgentSubscriberMessage(AvgAgent avgAgent, Topic topic, Node topicNode) {
        Double operand = getAvgAgentOperand(avgAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private Double getAvgAgentOperand(AvgAgent avgAgent, Topic topic) {
        if (topic.name.equals(avgAgent.getSubscribedTopics()[0])) {
            return avgAgent.getFirstOperand();
        }
        return avgAgent.getSecondOperand();
    }

    private void processIncAgentSubscriberMessage(IncAgent incAgent, Node topicNode) {
        Double operand = incAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private void processDecAgentSubscriberMessage(DecAgent decAgent, Node topicNode) {
        Double operand = decAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private void processNegAgentSubscriberMessage(NegAgent negAgent, Node topicNode) {
        Double operand = negAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private void processAbsAgentSubscriberMessage(AbsAgent absAgent, Node topicNode) {
        Double operand = absAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private void processDoubleAgentSubscriberMessage(DoubleAgent doubleAgent, Node topicNode) {
        Double operand = doubleAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private void processPublisherMessage(Agent publisher, Topic topic, Node topicNode) {
        if (publisher instanceof PlusAgent) {
            processPlusAgentPublisherMessage((PlusAgent) publisher, topicNode);
        } else if (publisher instanceof IncAgent) {
            processIncAgentPublisherMessage((IncAgent) publisher, topicNode);
        } else if (publisher instanceof MultAgent) {
            processMultAgentPublisherMessage((MultAgent) publisher, topicNode);
        } else if (publisher instanceof MinusAgent) {
            processMinusAgentPublisherMessage((MinusAgent) publisher, topicNode);
        } else if (publisher instanceof MaxAgent) {
            processMaxAgentPublisherMessage((MaxAgent) publisher, topicNode);
        } else if (publisher instanceof MinAgent) {
            processMinAgentPublisherMessage((MinAgent) publisher, topicNode);
        } else if (publisher instanceof AvgAgent) {
            processAvgAgentPublisherMessage((AvgAgent) publisher, topicNode);
        } else if (publisher instanceof DecAgent) {
            processDecAgentPublisherMessage((DecAgent) publisher, topicNode);
        } else if (publisher instanceof NegAgent) {
            processNegAgentPublisherMessage((NegAgent) publisher, topicNode);
        } else if (publisher instanceof AbsAgent) {
            processAbsAgentPublisherMessage((AbsAgent) publisher, topicNode);
        } else if (publisher instanceof DoubleAgent) {
            processDoubleAgentPublisherMessage((DoubleAgent) publisher, topicNode);
        }
    }

    private void processPlusAgentPublisherMessage(PlusAgent plusAgent, Node topicNode) {
        Double result = plusAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processIncAgentPublisherMessage(IncAgent incAgent, Node topicNode) {
        Double result = incAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processMultAgentPublisherMessage(MultAgent multAgent, Node topicNode) {
        Double result = multAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processMinusAgentPublisherMessage(MinusAgent minusAgent, Node topicNode) {
        Double result = minusAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processMaxAgentPublisherMessage(MaxAgent maxAgent, Node topicNode) {
        Double result = maxAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processMinAgentPublisherMessage(MinAgent minAgent, Node topicNode) {
        Double result = minAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processAvgAgentPublisherMessage(AvgAgent avgAgent, Node topicNode) {
        Double result = avgAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processDecAgentPublisherMessage(DecAgent decAgent, Node topicNode) {
        Double result = decAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processNegAgentPublisherMessage(NegAgent negAgent, Node topicNode) {
        Double result = negAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processAbsAgentPublisherMessage(AbsAgent absAgent, Node topicNode) {
        Double result = absAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    private void processDoubleAgentPublisherMessage(DoubleAgent doubleAgent, Node topicNode) {
        Double result = doubleAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }
}
