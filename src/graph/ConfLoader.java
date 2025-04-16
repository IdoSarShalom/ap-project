package graph;

import configs.GenericConfig;
import server.RequestParser;
import servlets.Servlet;
import views.HtmlGraphWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfLoader implements Servlet {
    private static final String TEMP_UPLOAD_PATH = "uploaded.conf";

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        String configContent = extractConfigContent(requestInfo);
        saveConfigToFile(configContent);
        clearTopicManager();
        createConfigFromFile();
        String graphHtml = generateGraphVisualization();
        sendGraphResponse(clientOutput, graphHtml);
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

    private String generateGraphVisualization() throws IOException {
        Graph graph = new Graph();
        graph.createFromTopics();
        return HtmlGraphWriter.getGraphHTML(graph);
    }

    private void sendGraphResponse(OutputStream clientOutput, String graphHtml) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + graphHtml.length() + "\r\n" +
                "Connection: close\r\n\r\n" +
                graphHtml;
        clientOutput.write(response.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(Paths.get(TEMP_UPLOAD_PATH));
    }
}