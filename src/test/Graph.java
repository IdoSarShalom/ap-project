package test;

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

        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

        for (Topic topic : topicManager.getTopics()) {
            Node topicNode = retrieveOrCreateTopicNode(topic, topicNodeMap);
            connectSubscribersToTopic(topic, topicNode, agentNodeMap);
            connectPublishersToTopic(topic, topicNode, agentNodeMap);
        }
    }

    private void clearGraph() {
        clear();
    }

    private Node retrieveOrCreateTopicNode(Topic topic, Map<Topic, Node> topicNodeMap) {
        return topicNodeMap.computeIfAbsent(topic, t -> {
            Node newNode = new Node(String.format("T%s", t.name));
            this.add(newNode);
            return newNode;
        });
    }

    private Node retrieveOrCreateAgentNode(Agent agent, Map<Agent, Node> agentNodeMap) {
        return agentNodeMap.computeIfAbsent(agent, a -> {
            Node newNode = new Node(String.format("A%s", a.getName()));
            this.add(newNode);
            return newNode;
        });
    }

    private void connectSubscribersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent subscriber : topic.getSubscribers()) {
            Node agentNode = retrieveOrCreateAgentNode(subscriber, agentNodeMap);
            topicNode.addEdge(agentNode);
        }
    }

    private void connectPublishersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent publisher : topic.getPublishers()) {
            Node agentNode = retrieveOrCreateAgentNode(publisher, agentNodeMap);
            agentNode.addEdge(topicNode);
        }
    }
}
