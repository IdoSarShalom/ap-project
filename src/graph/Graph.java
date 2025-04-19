package graph;

import configs.Agent;
import configs.Node;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A graph representation of a publish-subscribe topology.
 * This class extends ArrayList to store nodes and provides methods for
 * creating and analyzing graphs that represent message flow between
 * agents and topics in a publish-subscribe system.
 * 
 * The graph consists of nodes representing topics and agents, with
 * edges representing the message flow between them.
 */
public class Graph extends ArrayList<Node> {

    /**
     * Checks if the graph contains any cycles.
     * 
     * @return true if the graph contains cycles, false otherwise
     */
    public boolean hasCycles() {
        return this.stream().anyMatch(Node::hasCycles);
    }

    /**
     * Creates a graph from topics in the topic manager.
     * This method builds a complete graph representation of the
     * publish-subscribe system by creating nodes for topics and agents,
     * and connecting them according to their publish/subscribe relationships.
     */
    public void createFromTopics() {
        clearGraph();
        Map<Topic, Node> topicNodeMap = new ConcurrentHashMap<>();
        Map<Agent, Node> agentNodeMap = new ConcurrentHashMap<>();
        processTopics(topicNodeMap, agentNodeMap);
    }

    /**
     * Clears all nodes and edges from the graph.
     */
    private void clearGraph() {
        clear();
    }

    /**
     * Processes all topics to build the graph.
     * Creates nodes for topics and agents, and connects them based on 
     * publish/subscribe relationships.
     * 
     * @param topicNodeMap mapping between topics and their corresponding nodes
     * @param agentNodeMap mapping between agents and their corresponding nodes
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
     * Retrieves an existing node for a topic or creates a new one if it doesn't exist.
     * 
     * @param topic the topic for which to retrieve or create a node
     * @param topicNodeMap mapping between topics and their corresponding nodes
     * @return the node corresponding to the topic
     */
    private Node retrieveOrCreateTopicNode(Topic topic, Map<Topic, Node> topicNodeMap) {
        return topicNodeMap.computeIfAbsent(topic, t -> createNewNode(t.name));
    }

    /**
     * Retrieves an existing node for an agent or creates a new one if it doesn't exist.
     * 
     * @param agent the agent for which to retrieve or create a node
     * @param agentNodeMap mapping between agents and their corresponding nodes
     * @return the node corresponding to the agent
     */
    private Node retrieveOrCreateAgentNode(Agent agent, Map<Agent, Node> agentNodeMap) {
        return agentNodeMap.computeIfAbsent(agent, a -> createNewNode(a.getName()));
    }

    /**
     * Creates a new node with the given name and adds it to the graph.
     * 
     * @param name the name of the new node
     * @return the newly created node
     */
    private Node createNewNode(String name) {
        Node newNode = new Node(name);
        this.add(newNode);
        return newNode;
    }

    /**
     * Connects subscribers to a topic node in the graph.
     * For each subscriber to the topic, creates a connection from the topic to the subscriber
     * and processes any message information.
     * 
     * @param topic the topic whose subscribers should be connected
     * @param topicNode the node representing the topic
     * @param agentNodeMap mapping between agents and their corresponding nodes
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
     * For each publisher to the topic, creates a connection from the publisher to the topic
     * and processes any message information.
     * 
     * @param topic the topic whose publishers should be connected
     * @param topicNode the node representing the topic
     * @param agentNodeMap mapping between agents and their corresponding nodes
     */
    private void connectPublishersToTopic(Topic topic, Node topicNode, Map<Agent, Node> agentNodeMap) {
        for (Agent publisher : topic.getPublishers()) {
            Node agentNode = retrieveOrCreateAgentNode(publisher, agentNodeMap);
            agentNode.addEdge(topicNode);
            processPublisherMessage(publisher, topic, topicNode);
        }
    }

    /**
     * Processes message information for a subscriber agent.
     * Delegates to specific processing methods based on the type of agent.
     * 
     * @param subscriber the subscriber agent
     * @param topic the topic the agent subscribes to
     * @param topicNode the node representing the topic
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

    /**
     * Processes message information for a PlusAgent subscriber.
     * 
     * @param plusAgent the PlusAgent subscriber
     * @param topic the topic the agent subscribes to
     * @param topicNode the node representing the topic
     */
    private void processPlusAgentSubscriberMessage(PlusAgent plusAgent, Topic topic, Node topicNode) {
        Double operand = getPlusAgentOperand(plusAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand for a PlusAgent based on the topic.
     * 
     * @param plusAgent the PlusAgent
     * @param topic the topic
     * @return the operand value for the given topic
     */
    private Double getPlusAgentOperand(PlusAgent plusAgent, Topic topic) {
        if (topic.name.equals(plusAgent.getSubscribedTopics()[0])) {
            return plusAgent.getFirstOperand();
        }
        return plusAgent.getSecondOperand();
    }

    /**
     * Processes message information for a MultAgent subscriber.
     * 
     * @param multAgent the MultAgent subscriber
     * @param topic the topic the agent subscribes to
     * @param topicNode the node representing the topic
     */
    private void processMultAgentSubscriberMessage(MultAgent multAgent, Topic topic, Node topicNode) {
        Double operand = getMultAgentOperand(multAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand for a MultAgent based on the topic.
     * 
     * @param multAgent the MultAgent
     * @param topic the topic
     * @return the operand value for the given topic
     */
    private Double getMultAgentOperand(MultAgent multAgent, Topic topic) {
        if (topic.name.equals(multAgent.getSubscribedTopics()[0])) {
            return multAgent.getFirstOperand();
        }
        return multAgent.getSecondOperand();
    }

    /**
     * Processes message information for a MinusAgent subscriber.
     * 
     * @param minusAgent the MinusAgent subscriber
     * @param topic the topic the agent subscribes to
     * @param topicNode the node representing the topic
     */
    private void processMinusAgentSubscriberMessage(MinusAgent minusAgent, Topic topic, Node topicNode) {
        Double operand = getMinusAgentOperand(minusAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand for a MinusAgent based on the topic.
     * 
     * @param minusAgent the MinusAgent
     * @param topic the topic
     * @return the operand value for the given topic
     */
    private Double getMinusAgentOperand(MinusAgent minusAgent, Topic topic) {
        if (topic.name.equals(minusAgent.getSubscribedTopics()[0])) {
            return minusAgent.getFirstOperand();
        }
        return minusAgent.getSecondOperand();
    }

    /**
     * Processes message information for a MaxAgent subscriber.
     * 
     * @param maxAgent the MaxAgent subscriber
     * @param topic the topic the agent subscribes to
     * @param topicNode the node representing the topic
     */
    private void processMaxAgentSubscriberMessage(MaxAgent maxAgent, Topic topic, Node topicNode) {
        Double operand = getMaxAgentOperand(maxAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand for a MaxAgent based on the topic.
     * 
     * @param maxAgent the MaxAgent
     * @param topic the topic
     * @return the operand value for the given topic
     */
    private Double getMaxAgentOperand(MaxAgent maxAgent, Topic topic) {
        if (topic.name.equals(maxAgent.getSubscribedTopics()[0])) {
            return maxAgent.getFirstOperand();
        }
        return maxAgent.getSecondOperand();
    }

    /**
     * Processes message information for a MinAgent subscriber.
     * 
     * @param minAgent the MinAgent subscriber
     * @param topic the topic the agent subscribes to
     * @param topicNode the node representing the topic
     */
    private void processMinAgentSubscriberMessage(MinAgent minAgent, Topic topic, Node topicNode) {
        Double operand = getMinAgentOperand(minAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand for a MinAgent based on the topic.
     * 
     * @param minAgent the MinAgent
     * @param topic the topic
     * @return the operand value for the given topic
     */
    private Double getMinAgentOperand(MinAgent minAgent, Topic topic) {
        if (topic.name.equals(minAgent.getSubscribedTopics()[0])) {
            return minAgent.getFirstOperand();
        }
        return minAgent.getSecondOperand();
    }

    /**
     * Processes message information for an AvgAgent subscriber.
     * 
     * @param avgAgent the AvgAgent subscriber
     * @param topic the topic the agent subscribes to
     * @param topicNode the node representing the topic
     */
    private void processAvgAgentSubscriberMessage(AvgAgent avgAgent, Topic topic, Node topicNode) {
        Double operand = getAvgAgentOperand(avgAgent, topic);
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Gets the appropriate operand for an AvgAgent based on the topic.
     * 
     * @param avgAgent the AvgAgent
     * @param topic the topic
     * @return the operand value for the given topic
     */
    private Double getAvgAgentOperand(AvgAgent avgAgent, Topic topic) {
        if (topic.name.equals(avgAgent.getSubscribedTopics()[0])) {
            return avgAgent.getFirstOperand();
        }
        return avgAgent.getSecondOperand();
    }

    /**
     * Processes message information for an IncAgent subscriber.
     * 
     * @param incAgent the IncAgent subscriber
     * @param topicNode the node representing the topic
     */
    private void processIncAgentSubscriberMessage(IncAgent incAgent, Node topicNode) {
        Double operand = incAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes message information for a DecAgent subscriber.
     * 
     * @param decAgent the DecAgent subscriber
     * @param topicNode the node representing the topic
     */
    private void processDecAgentSubscriberMessage(DecAgent decAgent, Node topicNode) {
        Double operand = decAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes message information for a NegAgent subscriber.
     * 
     * @param negAgent the NegAgent subscriber
     * @param topicNode the node representing the topic
     */
    private void processNegAgentSubscriberMessage(NegAgent negAgent, Node topicNode) {
        Double operand = negAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes message information for an AbsAgent subscriber.
     * 
     * @param absAgent the AbsAgent subscriber
     * @param topicNode the node representing the topic
     */
    private void processAbsAgentSubscriberMessage(AbsAgent absAgent, Node topicNode) {
        Double operand = absAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes message information for a DoubleAgent subscriber.
     * 
     * @param doubleAgent the DoubleAgent subscriber
     * @param topicNode the node representing the topic
     */
    private void processDoubleAgentSubscriberMessage(DoubleAgent doubleAgent, Node topicNode) {
        Double operand = doubleAgent.getOperand();
        if (operand != null) {
            topicNode.setMessage(new Message(operand));
        }
    }

    /**
     * Processes message information for a publisher agent.
     * Delegates to specific processing methods based on the type of agent.
     * 
     * @param publisher the publisher agent
     * @param topic the topic the agent publishes to
     * @param topicNode the node representing the topic
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
     * Processes message information for a PlusAgent publisher.
     * 
     * @param plusAgent the PlusAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processPlusAgentPublisherMessage(PlusAgent plusAgent, Node topicNode) {
        Double result = plusAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for an IncAgent publisher.
     * 
     * @param incAgent the IncAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processIncAgentPublisherMessage(IncAgent incAgent, Node topicNode) {
        Double result = incAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for a MultAgent publisher.
     * 
     * @param multAgent the MultAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processMultAgentPublisherMessage(MultAgent multAgent, Node topicNode) {
        Double result = multAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for a MinusAgent publisher.
     * 
     * @param minusAgent the MinusAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processMinusAgentPublisherMessage(MinusAgent minusAgent, Node topicNode) {
        Double result = minusAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for a MaxAgent publisher.
     * 
     * @param maxAgent the MaxAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processMaxAgentPublisherMessage(MaxAgent maxAgent, Node topicNode) {
        Double result = maxAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for a MinAgent publisher.
     * 
     * @param minAgent the MinAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processMinAgentPublisherMessage(MinAgent minAgent, Node topicNode) {
        Double result = minAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for an AvgAgent publisher.
     * 
     * @param avgAgent the AvgAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processAvgAgentPublisherMessage(AvgAgent avgAgent, Node topicNode) {
        Double result = avgAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for a DecAgent publisher.
     * 
     * @param decAgent the DecAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processDecAgentPublisherMessage(DecAgent decAgent, Node topicNode) {
        Double result = decAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for a NegAgent publisher.
     * 
     * @param negAgent the NegAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processNegAgentPublisherMessage(NegAgent negAgent, Node topicNode) {
        Double result = negAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for an AbsAgent publisher.
     * 
     * @param absAgent the AbsAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processAbsAgentPublisherMessage(AbsAgent absAgent, Node topicNode) {
        Double result = absAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }

    /**
     * Processes message information for a DoubleAgent publisher.
     * 
     * @param doubleAgent the DoubleAgent publisher
     * @param topicNode the node representing the topic
     */
    private void processDoubleAgentPublisherMessage(DoubleAgent doubleAgent, Node topicNode) {
        Double result = doubleAgent.getResult();
        if (result != null) {
            topicNode.setMessage(new Message(result));
        }
    }
}
