package servlets;

import configs.GenericConfig;
import graph.Graph;
import graph.TopicManagerSingleton;
import server.RequestParser;
import views.HtmlGraphWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfLoader implements Servlet {
    private static final String UPLOADED_CONF_PATH = "uploaded.conf";

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        String configContent = extractConfigContent(requestInfo);
        saveConfigToFile(configContent);
        clearTopicManager();

        try {
            createConfigFromFile();
            String graphHtml = generateGraphVisualization();
            sendGraphResponse(clientOutput, graphHtml);
        } catch (RuntimeException e) {
            sendErrorResponse(clientOutput, e.getMessage());
        } catch (IOException e) {
            sendErrorResponse(clientOutput, "Internal server error: " + e.getMessage());
        }
    }

    private String extractConfigContent(RequestParser.RequestInfo requestInfo) {
        return new String(requestInfo.getContent(), StandardCharsets.UTF_8);
    }

    private void saveConfigToFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(UPLOADED_CONF_PATH)) {
            writer.write(content);
        }
    }

    private void clearTopicManager() {
        TopicManagerSingleton.get().clear();
    }

    private void createConfigFromFile() {
        GenericConfig config = new GenericConfig();
        config.setConfFile(UPLOADED_CONF_PATH);
        config.create();
    }

    private String generateGraphVisualization() throws IOException {
        Graph graph = new Graph();
        graph.createFromTopics();

        if (graph.hasCycles()) {
            throw new RuntimeException("Invalid: The current configuration graph contains cycles. " +
                    "Please provide a non-cyclic configuration.");
        }

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

    private void sendErrorResponse(OutputStream clientOutput, String errorMessage) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + errorMessage.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                errorMessage;
        clientOutput.write(response.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(Paths.get(UPLOADED_CONF_PATH));
    }
}