package views;

import configs.Node;
import graph.Graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlGraphWriter {

    public static String getGraphHTML(Graph graph) throws IOException {
        String graphJson = toCytoscapeJson(graph);
        return "<html>\n" +
                "<head>\n" +
                "  <script src=\"cytoscape.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"cy\"></div>\n" +
                "  <script>\n" +
                "    var elements = " + graphJson + ";\n" +
                "    var cy = cytoscape({\n" +
                "      container: document.getElementById('cy'),\n" +
                "      elements: elements\n" +
                "    });\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String toCytoscapeJson(Graph graph) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (Node node : graph) {
            Map<String, Object> nodeObj = new HashMap<>();
            nodeObj.put("id", node.getName());
            nodeObj.put("label", node.getName());
            if (node.getName().startsWith("T")) {
                nodeObj.put("value", 0.0);
            }
            nodes.add(nodeObj);
        }

        for (Node node : graph) {
            for (Node neighbor : node.getEdges()) {
                Map<String, Object> edgeObj = new HashMap<>();
                edgeObj.put("from", node.getName());
                edgeObj.put("to", neighbor.getName());
                if (node.getName().startsWith("A") && neighbor.getName().startsWith("T")) {
                    edgeObj.put("weight", 0.0); // Placeholder; replace with actual weight
                }
                edges.add(edgeObj);
            }
        }

        Map<String, Object> elements = new HashMap<>();
        elements.put("nodes", nodes);
        elements.put("edges", edges);

        return toJson(elements);
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