package test;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private String name;
    private List<Node> edges;
    private Message message;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
        this.message = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getEdges() {
        return edges;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void addEdge(Node node) {
        if (!this.edges.contains(node)) {
            this.edges.add(node);
        }
    }

    public boolean hasCycles() {
        return hasCyclesDfs(new ArrayList<>(), new ArrayList<>());
    }

    private boolean hasCyclesDfs(List<Node> visited, List<Node> stack) {
        if (!visited.contains(this)) {
            visited.add(this);
            stack.add(this);

            for (Node neighbor : edges) {
                if (!visited.contains(neighbor)) {
                    if (neighbor.hasCyclesDfs(visited, stack)) {
                        return true;
                    }
                } else if (stack.contains(neighbor)) {
                    return true;
                }
            }
        }

        stack.remove(this);
        return false;
    }
}
