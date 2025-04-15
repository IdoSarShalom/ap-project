package views;

import configs.Node;
import graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlGraphWriter {

    public static String getGraphHTML(Graph graph) {
        return toGraphJson(graph);
    }

    private static String toGraphJson(Graph graph) {
        List<Node> nodes = collectNodes(graph);
        Map<Node, Integer> nodeIdMap = assignNodeIds(nodes);
        List<Map<String, Object>> nodeData = buildNodeData(nodes, nodeIdMap);
        List<Map<String, Object>> edgeData = buildEdgeData(nodes, nodeIdMap);

        Map<String, Object> elements = new HashMap<>();
        elements.put("nodes", nodeData);
        elements.put("edges", edgeData);

        return toJson(elements);
    }

    private static List<Node> collectNodes(Graph graph) {
        List<Node> nodes = new ArrayList<>();
        for (Node node : graph) {
            nodes.add(node);
        }
        return nodes;
    }

    private static Map<Node, Integer> assignNodeIds(List<Node> nodes) {
        Map<Node, Integer> nodeIdMap = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            nodeIdMap.put(nodes.get(i), i + 1);
        }
        return nodeIdMap;
    }

    private static List<Map<String, Object>> buildNodeData(List<Node> nodes, Map<Node, Integer> nodeIdMap) {
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

    private static List<Map<String, Object>> buildEdgeData(List<Node> nodes, Map<Node, Integer> nodeIdMap) {
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
            json.append(toJson(entry.getValue()));
            first = false;
        }

        json.append("}");
        return json.toString();
    }

    private static String toJson(Object value) {
        if (value instanceof Map) {
            return toJson((Map<String, Object>) value);
        } else if (value instanceof List) {
            return toJson((List<?>) value);
        } else if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            return "null";
        }
    }

    private static String toJson(List<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        boolean first = true;
        for (Object item : list) {
            if (!first) {
                json.append(",");
            }
            json.append(toJson(item));
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