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

public class HtmlGraphWriter {
    private static final String GRAPH_PATH = "web/graph.html";

    public static String getGraphHTML(Graph graph) throws IOException {
        String template = readTemplate();
        String jsonGraph = toGraphJson(graph);

        return template.replace("{{GRAPH_DATA}}", jsonGraph);
    }

    private static String readTemplate() throws IOException {
        return Files.readString(Paths.get(GRAPH_PATH));
    }

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

    private static List<Node> getNodes(Graph graph) {
        return new ArrayList<>(graph);
    }

    private static Map<Node, Integer> assignNodeIds(List<Node> nodes) {
        Map<Node, Integer> nodeIdMap = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            nodeIdMap.put(nodes.get(i), i + 1);
        }

        return nodeIdMap;
    }

    private static List<Map<String, Object>> createNodeData(List<Node> nodes, Map<Node, Integer> nodeIdMap) {
        List<Map<String, Object>> nodeData = new ArrayList<>();

        for (Node node : nodes) {
            Map<String, Object> nodeObj = new HashMap<>();
            int id = nodeIdMap.get(node);
            nodeObj.put("id", id);
            nodeObj.put("label", node.getName());
            nodeData.add(nodeObj);
        }

        return nodeData;
    }

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