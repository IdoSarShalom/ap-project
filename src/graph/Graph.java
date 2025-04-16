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
            connectSubscribersToTopic(topic, topicNode, agentNodeMap);
            connectPublishersToTopic(topic, topicNode, agentNodeMap);
        }
    }

    private Node retrieveOrCreateTopicNode(Topic topic, Map<Topic, Node> topicNodeMap) {
        return topicNodeMap.computeIfAbsent(topic, t -> createNewNode(t.name));
    }

    private Node retrieveOrCreateAgentNode(Agent agent, Map<Agent, Node> agentNodeMap) {
        return agentNodeMap.computeIfAbsent(agent, a -> createNewNode(a.getName()));
    }

    private Node createNewNode(String name) {
        Node newNode = new Node(String.format("%s", name));
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

    private void processIncAgentSubscriberMessage(IncAgent incAgent, Node topicNode) {
        Double operand = incAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    private void processPublisherMessage(Agent publisher, Topic topic, Node topicNode) {
        if (publisher instanceof PlusAgent) {
            processPlusAgentPublisherMessage((PlusAgent) publisher, topicNode);
        } else if (publisher instanceof IncAgent) {
            processIncAgentPublisherMessage((IncAgent) publisher, topicNode);
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
}