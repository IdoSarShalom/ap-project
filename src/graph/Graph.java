package graph;

import configs.Agent;
import configs.Node;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the connectivity graph of the pub/sub system.
 * <p>
 * This class is responsible for creating a visual representation of the
 * pub/sub system topology, showing the connections between topics and agents.
 * It extends ArrayList to store Node objects representing both topics and agents
 * in the system, along with their connections.
 * <p>
 * The graph can be created from the current state of the TopicManager and can
 * check for cycles in the connection topology, which would indicate an invalid
 * configuration that could lead to infinite message loops.
 */
public class Graph extends ArrayList<Node> {

    /**
     * Checks if the graph contains any cycles.
     * <p>
     * Cycles in the pub/sub system would cause infinite message loops and are
     * considered invalid configurations.
     *
     * @return true if the graph contains cycles, false otherwise
     */
    public boolean hasCycles() {
        return this.stream().anyMatch(Node::hasCycles);
    }

    /**
     * Creates a graph representation from the current state of the TopicManager.
     * <p>
     * This method clears any existing graph data and builds a new graph
     * showing the connections between topics and agents (publishers and subscribers).
     */
    public void createFromTopics() {
        clearGraph();
        Map<Topic, Node> topicNodeMap = new ConcurrentHashMap<>();
        Map<Agent, Node> agentNodeMap = new ConcurrentHashMap<>();
        processTopics(topicNodeMap, agentNodeMap);
    }

    /**
     * Clears all nodes from the graph.
     */
    private void clearGraph() {
        clear();
    }

    /**
     * Processes all topics in the TopicManager to build the graph.
     * <p>
     * For each topic, this method creates a node and connects it to
     * its publishers and subscribers.
     *
     * @param topicNodeMap Map of topics to their corresponding nodes
     * @param agentNodeMap Map of agents to their corresponding nodes
     */
    private void processTopics(Map<Topic, Node> topicNodeMap, Map<Agent, Node> agentNodeMap) {
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

        for (Topic topic : topicManager.getTopics()) {
            Node topicNode = retrieveOrCreateTopicNode(topic, topicNodeMap);
            connectPublishersToTopic(topic, topicNode, agentNodeMap);
            connectSubscribersToTopic(topic, topicNode, agentNodeMap);
        }
    }

    /**
     * Retrieves an existing node for a topic or creates a new one.
     *
     * @param topic The topic to get or create a node for
     * @param topicNodeMap Map of topics to their corresponding nodes
     * @return The node representing the topic
     */
    private Node retrieveOrCreateTopicNode(Topic topic, Map<Topic, Node> topicNodeMap) {
        return topicNodeMap.computeIfAbsent(topic, t -> createNewNode(t.name));
    }

    /**
     * Retrieves an existing node for an agent or creates a new one.
     *
     * @param agent The agent to get or create a node for
     * @param agentNodeMap Map of agents to their corresponding nodes
     * @return The node representing the agent
     */
    private Node retrieveOrCreateAgentNode(Agent agent, Map<Agent, Node> agentNodeMap) {
        return agentNodeMap.computeIfAbsent(agent, a -> createNewNode(a.getName()));
    }

    /**
     * Creates a new node with the given name and adds it to the graph.
     *
     * @param name The name of the node
     * @return The newly created node
     */
    private Node createNewNode(String name) {
        Node newNode = new Node(name);
        this.add(newNode);
        return newNode;
    }

    /**
     * Connects subscribers to a topic node in the graph.
     * <p>
     * For each subscriber, this method creates a directed edge from
     * the topic to the subscriber and processes any messages.
     *
     * @param topic The topic being processed
     * @param topicNode The node representing the topic
     * @param agentNodeMap Map of agents to their corresponding nodes
     */
    private void connectSubscribersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent subscriber : topic.getSubscribers()) {
            Node agentNode = retrieveOrCreateAgentNode(subscriber, agentNodeMap);
            topicNode.addEdge(agentNode);
            processSubscriberMessage(subscriber, topic, topicNode);
        }
    }

    /**
     * Connects publishers to a topic node in the graph.
     * <p>
     * For each publisher, this method creates a directed edge from
     * the publisher to the topic and processes any messages.
     *
     * @param topic The topic being processed
     * @param topicNode The node representing the topic
     * @param agentNodeMap Map of agents to their corresponding nodes
     */
    private void connectPublishersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent publisher : topic.getPublishers()) {
            Node agentNode = retrieveOrCreateAgentNode(publisher, agentNodeMap);
            agentNode.addEdge(topicNode);
            processPublisherMessage(publisher, topic, topicNode);
        }
    }

    /**
     * Processes messages for a subscriber agent.
     * <p>
     * This method determines the type of the subscriber agent and delegates
     * to the appropriate handler method.
     *
     * @param subscriber The subscriber agent
     * @param topic The topic the agent is subscribed to
     * @param topicNode The node representing the topic
     */
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
            processMaxAgentSubscriberMessage((MaxAgent) subscriber, topicNode);
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

    /**
     * Processes messages for a PlusAgent subscriber.
     *
     * @param plusAgent The PlusAgent subscriber
     * @param topic The topic the agent is subscribed to
     * @param topicNode The node representing the topic
     */
    private void processPlusAgentSubscriberMessage(PlusAgent plusAgent, Topic topic, Node topicNode) {
        Double operand = getPlusAgentOperand(plusAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand from a PlusAgent based on the topic.
     *
     * @param plusAgent The PlusAgent
     * @param topic The topic
     * @return The operand value or null if not set
     */
    private Double getPlusAgentOperand(PlusAgent plusAgent, Topic topic) {
        if (topic.name.equals(plusAgent.getSubscribedTopics()[0])) {
            return plusAgent.getFirstOperand();
        }
        return plusAgent.getSecondOperand();
    }

    /**
     * Processes messages for a MultAgent subscriber.
     *
     * @param multAgent The MultAgent subscriber
     * @param topic The topic the agent is subscribed to
     * @param topicNode The node representing the topic
     */
    private void processMultAgentSubscriberMessage(MultAgent multAgent, Topic topic, Node topicNode) {
        Double operand = getMultAgentOperand(multAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand from a MultAgent based on the topic.
     *
     * @param multAgent The MultAgent
     * @param topic The topic
     * @return The operand value or null if not set
     */
    private Double getMultAgentOperand(MultAgent multAgent, Topic topic) {
        if (topic.name.equals(multAgent.getSubscribedTopics()[0])) {
            return multAgent.getFirstOperand();
        }
        return multAgent.getSecondOperand();
    }

    /**
     * Processes messages for a MinusAgent subscriber.
     *
     * @param minusAgent The MinusAgent subscriber
     * @param topic The topic the agent is subscribed to
     * @param topicNode The node representing the topic
     */
    private void processMinusAgentSubscriberMessage(MinusAgent minusAgent, Topic topic, Node topicNode) {
        Double operand = getMinusAgentOperand(minusAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand from a MinusAgent based on the topic.
     *
     * @param minusAgent The MinusAgent
     * @param topic The topic
     * @return The operand value or null if not set
     */
    private Double getMinusAgentOperand(MinusAgent minusAgent, Topic topic) {
        if (topic.name.equals(minusAgent.getSubscribedTopics()[0])) {
            return minusAgent.getFirstOperand();
        }
        return minusAgent.getSecondOperand();
    }

    /**
     * Processes messages for a MaxAgent subscriber.
     *
     * @param maxAgent The MaxAgent subscriber
     * @param topicNode The node representing the topic
     */
    private void processMaxAgentSubscriberMessage(MaxAgent maxAgent, Node topicNode) {
        Double operand = maxAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes messages for a MinAgent subscriber.
     *
     * @param minAgent The MinAgent subscriber
     * @param topicNode The node representing the topic
     */
    private void processMinAgentSubscriberMessage(MinAgent minAgent, Node topicNode) {
        Double operand = minAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes messages for an AvgAgent subscriber.
     *
     * @param avgAgent The AvgAgent subscriber
     * @param topicNode The node representing the topic
     */
    private void processAvgAgentSubscriberMessage(AvgAgent avgAgent, Node topicNode) {
        Double operand = avgAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes messages for a DecAgent subscriber.
     *
     * @param decAgent The DecAgent subscriber
     * @param topicNode The node representing the topic
     */
    private void processDecAgentSubscriberMessage(DecAgent decAgent, Node topicNode) {
        Double operand = decAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes messages for a NegAgent subscriber.
     *
     * @param negAgent The NegAgent subscriber
     * @param topicNode The node representing the topic
     */
    private void processNegAgentSubscriberMessage(NegAgent negAgent, Node topicNode) {
        Double operand = negAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes messages for an AbsAgent subscriber.
     *
     * @param absAgent The AbsAgent subscriber
     * @param topicNode The node representing the topic
     */
    private void processAbsAgentSubscriberMessage(AbsAgent absAgent, Node topicNode) {
        Double operand = absAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes messages for a DoubleAgent subscriber.
     *
     * @param doubleAgent The DoubleAgent subscriber
     * @param topicNode The node representing the topic
     */
    private void processDoubleAgentSubscriberMessage(DoubleAgent doubleAgent, Node topicNode) {
        Double operand = doubleAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes messages for a publisher agent.
     * <p>
     * This method determines the type of the publisher agent and delegates
     * to the appropriate handler method.
     *
     * @param publisher The publisher agent
     * @param topic The topic the agent publishes to
     * @param topicNode The node representing the topic
     */
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

    /**
     * Processes messages for a PlusAgent publisher.
     * <p>
     * Sets the result of the PlusAgent operation as the message for the topic node.
     *
     * @param plusAgent The PlusAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processPlusAgentPublisherMessage(PlusAgent plusAgent, Node topicNode) {
        Double result = plusAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for an IncAgent publisher.
     * <p>
     * Sets the result of the IncAgent operation as the message for the topic node.
     *
     * @param incAgent The IncAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processIncAgentPublisherMessage(IncAgent incAgent, Node topicNode) {
        Double result = incAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for a MultAgent publisher.
     * <p>
     * Sets the result of the MultAgent operation as the message for the topic node.
     *
     * @param multAgent The MultAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processMultAgentPublisherMessage(MultAgent multAgent, Node topicNode) {
        Double result = multAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for a MinusAgent publisher.
     * <p>
     * Sets the result of the MinusAgent operation as the message for the topic node.
     *
     * @param minusAgent The MinusAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processMinusAgentPublisherMessage(MinusAgent minusAgent, Node topicNode) {
        Double result = minusAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for a MaxAgent publisher.
     * <p>
     * Sets the result of the MaxAgent operation as the message for the topic node.
     *
     * @param maxAgent The MaxAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processMaxAgentPublisherMessage(MaxAgent maxAgent, Node topicNode) {
        Double result = maxAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for a MinAgent publisher.
     * <p>
     * Sets the result of the MinAgent operation as the message for the topic node.
     *
     * @param minAgent The MinAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processMinAgentPublisherMessage(MinAgent minAgent, Node topicNode) {
        Double result = minAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for an AvgAgent publisher.
     * <p>
     * Sets the result of the AvgAgent operation as the message for the topic node.
     *
     * @param avgAgent The AvgAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processAvgAgentPublisherMessage(AvgAgent avgAgent, Node topicNode) {
        Double result = avgAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for a DecAgent publisher.
     * <p>
     * Sets the result of the DecAgent operation as the message for the topic node.
     *
     * @param decAgent The DecAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processDecAgentPublisherMessage(DecAgent decAgent, Node topicNode) {
        Double result = decAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for a NegAgent publisher.
     * <p>
     * Sets the result of the NegAgent operation as the message for the topic node.
     *
     * @param negAgent The NegAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processNegAgentPublisherMessage(NegAgent negAgent, Node topicNode) {
        Double result = negAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for an AbsAgent publisher.
     * <p>
     * Sets the result of the AbsAgent operation as the message for the topic node.
     *
     * @param absAgent The AbsAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processAbsAgentPublisherMessage(AbsAgent absAgent, Node topicNode) {
        Double result = absAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes messages for a DoubleAgent publisher.
     * <p>
     * Sets the result of the DoubleAgent operation as the message for the topic node.
     *
     * @param doubleAgent The DoubleAgent publisher
     * @param topicNode The node representing the topic
     */
    private void processDoubleAgentPublisherMessage(DoubleAgent doubleAgent, Node topicNode) {
        Double result = doubleAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Returns a string representation of the graph.
     * <p>
     * This method provides a summary of the graph's nodes and edges,
     * which can be useful for debugging or logging.
     *
     * @return A string representation of the graph
     */
    @Override
    public String toString() {
        // ... existing code ...
    }
}
