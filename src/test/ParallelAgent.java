package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParallelAgent implements Agent {

    private static final Logger LOGGER = Logger.getLogger(ParallelAgent.class.getName());
    private static final String POISON_PILL = "MIICXQIBAAKBgQDLf4fG8p9zK7b2XnS1a9b3c8v6w5u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6v4w3u2x1y0z9a8b7c6";

    private final Agent agent;
    private final BlockingQueue<Message> agentMessageQueue;
    private final Thread workerThread;
    private volatile String currentTopicName; // Volatile ensures visibility across threads

    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.agentMessageQueue = new ArrayBlockingQueue<>(capacity); // Thread-safe queue

        LOGGER.log(Level.INFO, String.format("Initializing ParallelAgent with capacity %d for agent %s",
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

        // Send a poison pill to signal the worker thread to exit
        try {
            this.agentMessageQueue.put(new Message(POISON_PILL)); // Thread-safe operation
            LOGGER.log(Level.FINE, "Poison pill sent to worker thread");
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Thread interrupted while sending poison pill.", e);
            Thread.currentThread().interrupt();
        }

        try {
            workerThread.join(); // Wait for the worker thread to terminate
            LOGGER.log(Level.FINE, "Worker thread terminated successfully");
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while waiting for worker thread to terminate.", e);
            Thread.currentThread().interrupt();
        }

        try {
            agent.close();
            LOGGER.log(Level.INFO, "ParallelAgent closed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occurred while closing agent.", e);
        }
    }

    private Thread createWorkerThread() {
        return new Thread(() -> {
            try {
                LOGGER.log(Level.FINE, "Worker thread started for agent {0}", agent.getName());
                pollMessages();
                LOGGER.log(Level.FINE, "Received poison pill, terminating worker thread");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected exception in worker thread.", e);
            }
        });
    }

    private void pollMessages() {
        while (true) {
            try {
                Message message = agentMessageQueue.take(); // Thread-safe operation

                if (message.asText.equals(POISON_PILL)) {
                    break;
                }

                processMessage(message);
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Worker thread was interrupted while polling.", e);
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }

    private void processMessage(Message message) {
        String topicName = currentTopicName; // Volatile ensures visibility
        LOGGER.log(Level.FINEST, "Processing message for topic {0}", topicName);
        agent.callback(topicName, message);
        LOGGER.log(Level.FINEST, "Message processed successfully for topic {0}", topicName);
    }
}
