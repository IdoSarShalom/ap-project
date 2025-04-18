package configs;

import graph.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A wrapper for an Agent that processes messages in a separate thread.
 * <p>
 * This class implements the Agent interface by delegating to an underlying agent,
 * but processes messages asynchronously in a separate worker thread. Messages are
 * placed in a queue and processed in order by the worker thread.
 * <p>
 * This design allows agents to process messages concurrently, improving throughput
 * in the pub/sub system when multiple messages are being processed simultaneously.
 */
public class ParallelAgent implements Agent {

    /** The underlying agent that will process messages */
    private final Agent agent;
    
    /** Queue that holds messages waiting to be processed */
    private final BlockingQueue<Message> agentMessageQueue;
    
    /** The worker thread that processes messages from the queue */
    private final Thread workerThread;
    
    /** Flag to signal the worker thread to shut down */
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);
    
    /** The name of the topic that the currently queued message belongs to */
    private volatile String currentTopicName;

    /**
     * Creates a new parallel agent that wraps the specified agent.
     * <p>
     * Initializes the message queue with the specified capacity and starts
     * the worker thread for processing messages.
     *
     * @param agent The underlying agent to delegate message processing to
     * @param capacity The maximum number of messages that can be queued
     */
    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.agentMessageQueue = new ArrayBlockingQueue<>(capacity);
        this.workerThread = createWorkerThread();
        this.workerThread.start();
    }

    /**
     * Gets the name of this agent.
     * <p>
     * Delegates to the underlying agent to get its name.
     *
     * @return The name of the underlying agent
     */
    @Override
    public String getName() {
        return agent.getName();
    }

    /**
     * Resets the state of this agent.
     * <p>
     * Delegates to the underlying agent to reset its state.
     */
    @Override
    public void reset() {
        agent.reset();
    }

    /**
     * Queues a message to be processed asynchronously.
     * <p>
     * Instead of processing the message immediately, this method stores the topic name
     * and puts the message in the queue for the worker thread to process.
     *
     * @param topicName The name of the topic the message was published to
     * @param msg The message to be processed
     */
    @Override
    public void callback(String topicName, Message msg) {

        try {
            this.currentTopicName = topicName;
            this.agentMessageQueue.put(msg);
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted while queuing a message.");
            e.printStackTrace(System.err);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Closes this agent and releases its resources.
     * <p>
     * Sets the close flag to signal the worker thread to shut down,
     * then waits for it to terminate before closing the underlying agent.
     */
    @Override
    public void close() {
        closeFlag.set(true);

        try {
            workerThread.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for worker thread to terminate.");
            e.printStackTrace(System.err);
            Thread.currentThread().interrupt();
        }

        agent.close();
    }

    /**
     * Creates the worker thread for processing messages.
     * <p>
     * The worker thread runs the pollMessages method to continuously
     * process messages from the queue until the close flag is set.
     *
     * @return A new Thread configured to process messages
     */
    private Thread createWorkerThread() {
        return new Thread(() -> {
            try {
                pollMessages();
            } catch (Exception e) {
                System.err.println("Unexpected exception in worker thread.");
                e.printStackTrace(System.err);
            }
        });
    }

    /**
     * Polls messages from the queue and processes them.
     * <p>
     * This method runs in the worker thread and continuously checks for
     * messages in the queue, processing them by calling the underlying agent's
     * callback method. It continues until the close flag is set.
     */
    private void pollMessages() {
        while (!closeFlag.get()) {
            try {
                Message message = agentMessageQueue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    processMessage(message);
                }
            } catch (InterruptedException e) {
                System.err.println("Worker thread was interrupted while polling.");
                e.printStackTrace(System.err);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Processes a single message by delegating to the underlying agent.
     * <p>
     * This method captures the current topic name and passes it along with
     * the message to the underlying agent's callback method.
     *
     * @param message The message to process
     */
    private void processMessage(Message message) {
        String topicName = currentTopicName;
        agent.callback(topicName, message);
    }
}