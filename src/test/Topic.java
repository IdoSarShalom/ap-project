package test;

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
        subs.forEach(agent -> agent.callback(this.name, msg));
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
