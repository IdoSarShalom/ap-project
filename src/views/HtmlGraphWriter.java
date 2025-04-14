package views;

import configs.Node;
import graph.Graph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlGraphWriter {

    public static void getGraphHTML(Graph graph, String filePath) throws IOException {
        String graphJson = toCytoscapeJson(graph);
        String htmlContent = "<html>\n" +
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
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(htmlContent);
        }
    }

    private static String toCytoscapeJson(Graph graph) {
        List<Map<String, Map<String, Object>>> nodes = new ArrayList<>();
        List<Map<String, Map<String, Object>>> edges = new ArrayList<>();

        for (Node node : graph) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", node.getName());
            data.put("label", node.getName());
            if (node.getName().startsWith("T")) {
                data.put("value", 0.0); // Placeholder; replace with actual topic value
            }
            Map<String, Map<String, Object>> nodeObj = new HashMap<>();
            nodeObj.put("data", data);
            nodes.add(nodeObj);
        }

        for (Node node : graph) {
            for (Node neighbor : node.getEdges()) {
                Map<String, Object> data = new HashMap<>();
                data.put("source", node.getName());
                data.put("target", neighbor.getName());
                if (node.getName().startsWith("A") && neighbor.getName().startsWith("T")) {
                    data.put("weight", 0.0); // Placeholder; replace with actual weight
                }
                Map<String, Map<String, Object>> edgeObj = new HashMap<>();
                edgeObj.put("data", data);
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
