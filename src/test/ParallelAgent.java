package test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParallelAgent implements Agent {

    private static final Logger LOGGER = Logger.getLogger(ParallelAgent.class.getName());

    private final Agent agent;
    private final BlockingQueue<QueuedAgentPayload> agentPayloadQueue;
    private final Thread workerThread;

    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.agentPayloadQueue = new ArrayBlockingQueue<>(capacity);
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
        try {
            LOGGER.log(Level.FINE, "Queueing message for topic {0}", topicName);
            this.agentPayloadQueue.put(new QueuedAgentPayload(topicName, msg));
            LOGGER.log(Level.FINEST, "Message queued successfully for topic {0}", topicName);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Thread interrupted while queuing a message.", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        LOGGER.log(Level.INFO, "Closing ParallelAgent for {0}", agent.getName());
        sendPoisonPill();
        awaitWorkerThreadTermination();
        closeAgent();
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
                QueuedAgentPayload payload = agentPayloadQueue.take();
                if (payload.getIsAgentClosing()) {
                    break;
                }
                processMessage(payload);
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Worker thread was interrupted while polling.", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processMessage(QueuedAgentPayload payload) {
        String topicName = payload.getTopicName();
        LOGGER.log(Level.FINEST, "Processing message for topic {0}", topicName);
        agent.callback(topicName, payload.getMessage());
        LOGGER.log(Level.FINEST, "Message processed successfully for topic {0}", topicName);
    }

    private void sendPoisonPill() {
        try {
            LOGGER.log(Level.FINE, "Sending poison pill to worker thread");
            agentPayloadQueue.put(new QueuedAgentPayload(true));
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Thread interrupted while sending poison pill.", e);
            workerThread.interrupt();
        }
    }

    private void awaitWorkerThreadTermination() {
        try {
            workerThread.join();
            LOGGER.log(Level.FINE, "Worker thread terminated successfully");
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while waiting for worker thread to terminate.", e);
            Thread.currentThread().interrupt();
        }
    }

    private void closeAgent() {
        try {
            agent.close();
            LOGGER.log(Level.INFO, "ParallelAgent closed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occurred while closing agent.", e);
        }
    }
}

class QueuedAgentPayload {
    private final String topicName;
    private final Message msg;
    private final boolean isAgentClosing;

    QueuedAgentPayload(String topic, Message msg) {
        this.topicName = topic;
        this.msg = msg;
        this.isAgentClosing = false;
    }

    QueuedAgentPayload(boolean isAgentClosing) {
        this.topicName = null;
        this.msg = null;
        this.isAgentClosing = isAgentClosing;
    }

    public String getTopicName() {
        return this.topicName;
    }

    public Message getMessage() {
        return this.msg;
    }

    public boolean getIsAgentClosing() {
        return this.isAgentClosing;
    }
}
