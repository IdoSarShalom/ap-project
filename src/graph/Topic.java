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
            switch (agent) {
                case PlusAgent plusAgent -> plusAgent.setResult(msg.asDouble);
                case IncAgent incAgent -> incAgent.setResult(msg.asDouble);
                default -> {}
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
