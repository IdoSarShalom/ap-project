
package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelAgent implements Agent {

    private static final Logger LOGGER = Logger.getLogger(ParallelAgent.class.getName());

    private final Agent agent;
    private final BlockingQueue<Message> agentMessageQueue;
    private final Thread workerThread;
    private final AtomicBoolean closeFlag = new AtomicBoolean(false); // Thread-safe flag
    private volatile String currentTopicName; // Volatile ensures visibility across threads

    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.agentMessageQueue = new ArrayBlockingQueue<>(capacity); // Thread-safe queue

        LOGGER.info(String.format("Initializing ParallelAgent with capacity %d for agent %s",
                capacity, agent.getName()));

        this.workerThread = createWorkerThread();
        this.workerThread.start();
        LOGGER.log(Level.FINE, "Worker thread started successfully");
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        LOGGER.log(Level.INFO, "Resetting agent {0}", agent.getName());
        agent.reset();
    }

    @Override
    public void callback(String topicName, Message msg) {
        LOGGER.log(Level.FINE, "Queueing message for topic {0}", topicName);

        try {
            this.currentTopicName = topicName; // Volatile ensures visibility
            this.agentMessageQueue.put(msg); // Thread-safe operation
            LOGGER.log(Level.FINEST, "Message queued successfully for topic {0}", topicName);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Thread interrupted while queuing a message.", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        LOGGER.log(Level.INFO, "Closing ParallelAgent for {0}", agent.getName());

        // Set flag to true to signal the worker thread to exit
        closeFlag.set(true); // Atomic operation
        LOGGER.log(Level.FINE, "Closing signal sent to worker thread");

        try {
            workerThread.join(); // Wait for the worker thread to terminate
            LOGGER.log(Level.FINE, "Worker thread terminated successfully");
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while waiting for worker thread to terminate.", e);
            Thread.currentThread().interrupt();
        }

        agent.close();
        LOGGER.log(Level.INFO, "ParallelAgent closed successfully");
    }

    private Thread createWorkerThread() {
        return new Thread(() -> {
            try {
                LOGGER.log(Level.FINE, "Worker thread started for agent {0}", agent.getName());
                pollMessages();
                LOGGER.log(Level.FINE, "Received closing signal, terminating worker thread");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected exception in worker thread.", e);
            }
        });
    }

    private void pollMessages() {
        while (!closeFlag.get()) { // Atomic operation
            try {
                Message message = agentMessageQueue.poll(100, TimeUnit.MILLISECONDS); // Thread-safe operation
                if (message != null) {
                    processMessage(message);
                }
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Worker thread was interrupted while polling.", e);
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }

    private void processMessage(Message message) {
        if (message == null) {
            LOGGER.log(Level.WARNING, "Received null message, skipping processing.");
            return;
        }
        String topicName = currentTopicName; // Volatile ensures visibility
        LOGGER.log(Level.FINEST, "Processing message for topic {0}", topicName);
        agent.callback(topicName, message);
        LOGGER.log(Level.FINEST, "Message processed successfully for topic {0}", topicName);
    }
}