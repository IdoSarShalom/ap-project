package graph;

import configs.Agent;

import java.util.concurrent.CopyOnWriteArrayList;

class Topic {

    public final String name;
    private final CopyOnWriteArrayList<Agent> subs;
    private final CopyOnWriteArrayList<Agent> pubs;

    Topic(String name) {
        this.name = name;
        this.subs = new CopyOnWriteArrayList<>();
        this.pubs = new CopyOnWriteArrayList<>();
    }

    public void subscribe(Agent agent) {
        subs.addIfAbsent(agent);
    }

    public void unsubscribe(Agent agent) {
        subs.remove(agent);
    }

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

    public void addPublisher(Agent agent) {
        pubs.addIfAbsent(agent);
    }

    public void removePublisher(Agent agent) {
        pubs.remove(agent);
    }

    public CopyOnWriteArrayList<Agent> getSubscribers() {
        return subs;
    }

    public CopyOnWriteArrayList<Agent> getPublishers() {
        return pubs;
    }

}
