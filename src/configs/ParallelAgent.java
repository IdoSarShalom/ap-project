package configs;

import graph.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ParallelAgent implements Agent {

    private final Agent agent;
    private final BlockingQueue<Message> agentMessageQueue;
    private final Thread workerThread;
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);
    private volatile String currentTopicName;

    public ParallelAgent(Agent agent, int capacity) {
        this.agent = agent;
        this.agentMessageQueue = new ArrayBlockingQueue<>(capacity);
        this.workerThread = createWorkerThread();
        this.workerThread.start();
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        agent.reset();
    }

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

    private void processMessage(Message message) {
        String topicName = currentTopicName;
        agent.callback(topicName, message);
    }
}