package configs;

import graph.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the pub/sub system graph.
 * <p>
 * This class models a node in the directed graph that represents the pub/sub system topology.
 * Nodes can represent topics or agents in the system, and they are connected by directed edges
 * that represent the flow of messages between them.
 * <p>
 * Each node has a name, a list of edges to other nodes, and can store a message.
 * Nodes are used to visualize the system and to check for cycles in the message flow.
 */
public class Node {

    /** The name of this node */
    private String name;
    
    /** The list of nodes this node has edges to */
    private List<Node> edges;
    
    /** The message associated with this node, if any */
    private Message message;

    /**
     * Creates a new node with the specified name.
     * 
     * @param name The name of the node
     */
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
        this.message = null;
    }

    /**
     * Gets the name of this node.
     * 
     * @return The name of this node
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this node.
     * 
     * @param name The new name for this node
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of nodes this node has edges to.
     * 
     * @return The list of connected nodes
     */
    public List<Node> getEdges() {
        return edges;
    }

    /**
     * Sets the list of nodes this node has edges to.
     * 
     * @param edges The new list of connected nodes
     */
    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    /**
     * Gets the message associated with this node.
     * 
     * @return The message, or null if no message is associated
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Sets the message associated with this node.
     * 
     * @param message The message to associate with this node
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Adds a directed edge from this node to the specified node.
     * <p>
     * If an edge to the specified node already exists, this method has no effect.
     * 
     * @param node The node to add an edge to
     */
    public void addEdge(Node node) {
        if (!this.edges.contains(node)) {
            this.edges.add(node);
        }
    }

    /**
     * Checks if there are any cycles in the graph starting from this node.
     * <p>
     * A cycle in the graph would indicate a circular dependency in the pub/sub system,
     * which could lead to infinite message propagation.
     * 
     * @return true if a cycle is detected, false otherwise
     */
    public boolean hasCycles() {
        return hasCyclesDfs(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Helper method that performs a depth-first search to detect cycles.
     * <p>
     * This method uses a recursive DFS with a visited list and a stack to detect cycles.
     * A cycle is detected if we encounter a node that is already in the current stack.
     * 
     * @param visited List of nodes that have been visited
     * @param stack List of nodes in the current DFS path
     * @return true if a cycle is detected, false otherwise
     */
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
