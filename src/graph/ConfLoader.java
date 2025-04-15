package graph;

import configs.GenericConfig;
import server.RequestParser;
import servlets.Servlet;
import views.HtmlGraphWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConfLoader implements Servlet {
    private static final String TEMP_UPLOAD_PATH = "uploaded.conf";
    private static final String GRAPH_HTML_PATH = "web/index.html";

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        String configContent = extractConfigContent(requestInfo);
        saveConfigToFile(configContent);
        clearTopicManager();
        createConfigFromFile();
        generateGraphVisualization();
        redirectToGraphView(clientOutput);
    }

    private String extractConfigContent(RequestParser.RequestInfo requestInfo) {
        return new String(requestInfo.getContent(), StandardCharsets.UTF_8);
    }

    private void saveConfigToFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(TEMP_UPLOAD_PATH)) {
            writer.write(content);
        }
    }

    private void clearTopicManager() {
        TopicManagerSingleton.get().clear();
    }

    private void createConfigFromFile() {
        GenericConfig config = new GenericConfig();
        config.setConfFile(TEMP_UPLOAD_PATH);
        config.create();
    }

    private void generateGraphVisualization() throws IOException {
        Graph graph = new Graph();
        graph.createFromTopics();
        HtmlGraphWriter.getGraphHTML(graph, GRAPH_HTML_PATH);
    }

    private void redirectToGraphView(OutputStream clientOutput) throws IOException {
        String redirectResponse = "HTTP/1.1 302 Found\r\n" +
                "Location: /app/index.html\r\n" +
                "Connection: close\r\n\r\n";
        clientOutput.write(redirectResponse.getBytes());
    }

    @Override
    public void close() throws IOException {
    }
}