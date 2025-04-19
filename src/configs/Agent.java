package configs;

import graph.Message;

/**
 * The Agent interface defines the contract for all agents in the pub/sub system.
 * <p>
 * An agent is a processing component that can subscribe to topics to receive messages,
 * perform operations on those messages, and publish results to other topics.
 * Different implementations of this interface provide various transformation
 * operations on message data.
 */
public interface Agent {
    /**
     * Gets the name of this agent.
     * 
     * @return A string representing the name of the agent
     */
    String getName();

    /**
     * Resets the agent's internal state.
     * <p>
     * This method is called when the agent needs to clear any accumulated state
     * or return to its initial configuration.
     */
    void reset();

    /**
     * Processes a message received from a subscribed topic.
     * <p>
     * This method is called by a topic when a new message is published to it
     * and this agent is subscribed to that topic.
     * 
     * @param topic The name of the topic the message was published to
     * @param msg The message that was published
     */
    void callback(String topic, Message msg);

    /**
     * Cleans up resources and connections when the agent is no longer needed.
     * <p>
     * This method should unsubscribe from topics and perform any necessary cleanup
     * to avoid resource leaks.
     */
    void close();
}
