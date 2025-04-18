package graph;

import configs.Agent;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a topic in the pub/sub system.
 * <p>
 * A topic is a named channel that agents can publish to or subscribe to.
 * It manages the list of subscribers and publishers, and handles message
 * distribution when new messages are published to the topic.
 * <p>
 * The class uses thread-safe collections to support concurrent operations.
 */
class Topic {

    /** The name of the topic */
    public final String name;
    
    /** Thread-safe list of agents that subscribe to this topic */
    private final CopyOnWriteArrayList<Agent> subs;
    
    /** Thread-safe list of agents that publish to this topic */
    private final CopyOnWriteArrayList<Agent> pubs;

    /**
     * Creates a new topic with the specified name.
     *
     * @param name The name of the topic
     */
    Topic(String name) {
        this.name = name;
        this.subs = new CopyOnWriteArrayList<>();
        this.pubs = new CopyOnWriteArrayList<>();
    }

    /**
     * Adds an agent as a subscriber to this topic.
     * <p>
     * If the agent is already subscribed, it won't be added again.
     *
     * @param agent The agent to subscribe
     */
    public void subscribe(Agent agent) {
        subs.addIfAbsent(agent);
    }

    /**
     * Removes an agent from the subscribers list.
     *
     * @param agent The agent to unsubscribe
     */
    public void unsubscribe(Agent agent) {
        subs.remove(agent);
    }

    /**
     * Publishes a message to all subscribers of this topic.
     * <p>
     * When a message is published, it is delivered to all subscribers via their
     * callback method. Publishers are also notified of the message with the
     * appropriate type-specific handling.
     *
     * @param msg The message to publish
     */
    public void publish(Message msg) {
        subs.forEach(agent -> agent.callback(name, msg));
        pubs.forEach(agent -> {
            if (agent instanceof PlusAgent) {
                ((PlusAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof IncAgent) {
                ((IncAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof MultAgent) {
                ((MultAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof MinusAgent) {
                ((MinusAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof MaxAgent) {
                ((MaxAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof MinAgent) {
                ((MinAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof AvgAgent) {
                ((AvgAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof DecAgent) {
                ((DecAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof NegAgent) {
                ((NegAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof AbsAgent) {
                ((AbsAgent) agent).setResult(msg.asDouble);
            } else if (agent instanceof DoubleAgent) {
                ((DoubleAgent) agent).setResult(msg.asDouble);
            }
        });
    }

    /**
     * Adds an agent as a publisher to this topic.
     * <p>
     * If the agent is already a publisher, it won't be added again.
     *
     * @param agent The agent to add as a publisher
     */
    public void addPublisher(Agent agent) {
        pubs.addIfAbsent(agent);
    }

    /**
     * Removes an agent from the publishers list.
     *
     * @param agent The agent to remove
     */
    public void removePublisher(Agent agent) {
        pubs.remove(agent);
    }

    /**
     * Gets the list of subscribers to this topic.
     *
     * @return A thread-safe list of subscriber agents
     */
    public CopyOnWriteArrayList<Agent> getSubscribers() {
        return subs;
    }

    /**
     * Gets the list of publishers to this topic.
     *
     * @return A thread-safe list of publisher agents
     */
    public CopyOnWriteArrayList<Agent> getPublishers() {
        return pubs;
    }

}
