package views;

import configs.Node;
import graph.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating HTML visualizations of the pub/sub system graph.
 * <p>
 * This class reads an HTML template file and injects JavaScript code to render
 * the graph visualization using vis.js. It converts the Graph object into a JSON
 * representation that can be used by the visualization library.
 */
public class HtmlGraphWriter {
    /** Path to the HTML template file for the graph visualization */
    private static final String GRAPH_PATH = "web/graph.html";

    /**
     * Generates an HTML visualization of the given graph.
     * <p>
     * This method converts the graph to a JSON representation and injects it
     * into the HTML template.
     *
     * @param graph The Graph object to visualize
     * @return An HTML string containing the graph visualization
     * @throws IOException If an error occurs while reading the template file
     */
    public static String getGraphHTML(Graph graph) throws IOException {
        String template = readTemplate();
        String jsonGraph = toGraphJson(graph);

        return template.replace("{{GRAPH_DATA}}", jsonGraph);
    }

    /**
     * Reads the HTML template file for the graph visualization.
     *
     * @return The contents of the template file as a string
     * @throws IOException If an error occurs while reading the file
     */
    private static String readTemplate() throws IOException {
        return Files.readString(Paths.get(GRAPH_PATH));
    }

    /**
     * Converts a Graph object to a JSON representation for visualization.
     * <p>
     * This method creates a JSON object with nodes and edges arrays that
     * can be used by the vis.js network visualization library.
     *
     * @param graph The Graph object to convert
     * @return A JSON string representing the graph
     */
    private static String toGraphJson(Graph graph) {
        List<Node> nodes = getNodes(graph);
        Map<Node, Integer> nodeIdMap = assignNodeIds(nodes);
        List<Map<String, Object>> nodeData = createNodeData(nodes, nodeIdMap);
        List<Map<String, Object>> edgeData = createEdgeData(nodes, nodeIdMap);
        Map<String, Object> elements = new HashMap<>();
        elements.put("nodes", nodeData);
        elements.put("edges", edgeData);

        return toJson(elements);
    }

    /**
     * Gets a list of nodes from the graph.
     *
     * @param graph The Graph object containing the nodes
     * @return A list of Node objects
     */
    private static List<Node> getNodes(Graph graph) {
        return new ArrayList<>(graph);
    }

    /**
     * Assigns unique numeric IDs to each node in the graph.
     * <p>
     * These IDs are used to identify nodes in the visualization.
     *
     * @param nodes The list of nodes to assign IDs to
     * @return A map from Node objects to their assigned IDs
     */
    private static Map<Node, Integer> assignNodeIds(List<Node> nodes) {
        Map<Node, Integer> nodeIdMap = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            nodeIdMap.put(nodes.get(i), i + 1);
        }

        return nodeIdMap;
    }

    /**
     * Creates data objects for nodes in the graph visualization.
     * <p>
     * Each node object contains an ID and a label with the node's name
     * and message value (if any).
     *
     * @param nodes The list of nodes to create data for
     * @param nodeIdMap A map from Node objects to their assigned IDs
     * @return A list of node data objects
     */
    private static List<Map<String, Object>> createNodeData(List<Node> nodes, Map<Node, Integer> nodeIdMap) {
        List<Map<String, Object>> nodeData = new ArrayList<>();

        for (Node node : nodes) {
            Map<String, Object> nodeObj = new HashMap<>();
            int id = nodeIdMap.get(node);
            nodeObj.put("id", id);
            nodeObj.put("label", getLabel(node));

            nodeData.add(nodeObj);
        }

        return nodeData;
    }

    /**
     * Gets a label for a node in the visualization.
     * <p>
     * The label includes the node's name and, if available, its message value.
     *
     * @param node The Node object to create a label for
     * @return A string label for the node
     */
    private static String getLabel(Node node) {
        String label = node.getName();

        if (node.getMessage() != null) {
            label += "\n" + node.getMessage().asDouble;
        }

        return label;
    }

    /**
     * Creates data objects for edges in the graph visualization.
     * <p>
     * Each edge object contains "from" and "to" properties indicating
     * the source and target nodes of the edge.
     *
     * @param nodes The list of nodes in the graph
     * @param nodeIdMap A map from Node objects to their assigned IDs
     * @return A list of edge data objects
     */
    private static List<Map<String, Object>> createEdgeData(List<Node> nodes, Map<Node, Integer> nodeIdMap) {
        List<Map<String, Object>> edgeData = new ArrayList<>();

        for (Node node : nodes) {
            int fromId = nodeIdMap.get(node);

            for (Node neighbor : node.getEdges()) {
                int toId = nodeIdMap.get(neighbor);
                Map<String, Object> edgeObj = new HashMap<>();
                edgeObj.put("from", fromId);
                edgeObj.put("to", toId);
                edgeData.add(edgeObj);
            }
        }

        return edgeData;
    }

    /**
     * Converts a Map to a JSON string.
     * <p>
     * This method handles the conversion of Map objects to JSON format
     * without using external libraries.
     *
     * @param map The Map to convert
     * @return A JSON string representation of the Map
     */
    private static String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(toJsonValue(entry.getValue()));
            first = false;
        }
        json.append("}");

        return json.toString();
    }

    /**
     * Converts a value to its JSON representation.
     * <p>
     * This method handles different types of values (Maps, Lists, Strings, etc.)
     * and converts them to their appropriate JSON representation.
     *
     * @param value The value to convert
     * @return A JSON string representation of the value
     */
    private static String toJsonValue(Object value) {

        if (value instanceof Map) {
            return toJson((Map<String, Object>) value);
        }

        if (value instanceof List) {
            return toJsonList((List<?>) value);
        }

        if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        return "null";
    }

    /**
     * Converts a List to a JSON array string.
     *
     * @param list The List to convert
     * @return A JSON array string representation of the List
     */
    private static String toJsonList(List<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        boolean first = true;

        for (Object item : list) {

            if (!first) {
                json.append(",");
            }
            json.append(toJsonValue(item));
            first = false;
        }
        json.append("]");

        return json.toString();
    }

    /**
     * Escapes special characters in a string for JSON.
     *
     * @param str The string to escape
     * @return The escaped string
     */
    private static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}