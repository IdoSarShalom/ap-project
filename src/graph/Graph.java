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
            Node newNode = new Node(String.format("%s", t.name));
            this.add(newNode);
            return newNode;
        });
    }

    private Node retrieveOrCreateAgentNode(Agent agent, Map<Agent, Node> agentNodeMap) {
        return agentNodeMap.computeIfAbsent(agent, a -> {
            Node newNode = new Node(String.format("%s", a.getName()));
            this.add(newNode);
            return newNode;
        });
    }

    private void connectSubscribersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent subscriber : topic.getSubscribers()) {

            Node agentNode = retrieveOrCreateAgentNode(subscriber, agentNodeMap);
            topicNode.addEdge(agentNode);

            if (subscriber instanceof PlusAgent) {
                PlusAgent plusAgent = (PlusAgent) subscriber;

                Double operand;
                if (topic.name.equals(plusAgent.getSubscribedTopics()[0])) {
                    operand = plusAgent.getFirstOperand();
                } else {
                    operand = plusAgent.getSecondOperand();
                }

                if (operand != null) {
                    topicNode.setMessage(new Message(operand));
                }


            } else if (subscriber instanceof IncAgent) {
                IncAgent incAgent = (IncAgent) subscriber;
                Double operand = incAgent.getOperand();
                if (operand != null) {
                    topicNode.setMessage(new Message(operand));
                }
            }

        }
    }

    private void connectPublishersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent publisher : topic.getPublishers()) {
            Node agentNode = retrieveOrCreateAgentNode(publisher, agentNodeMap);
            agentNode.addEdge(topicNode);
        }
    }
}
