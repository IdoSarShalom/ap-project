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

/**
 * Servlet responsible for loading and processing configuration files for the pub/sub system.
 * <p>
 * This servlet handles POST requests with configuration content, saves the configuration
 * to a file, creates the configured agents and topics, and returns an HTML visualization
 * of the resulting graph. It also performs validation to ensure the configuration doesn't
 * create cycles in the graph.
 */
public class ConfLoader implements Servlet {
    /** The path where the uploaded configuration file is saved */
    private static final String UPLOADED_CONF_PATH = "uploaded.conf";

    /**
     * Handles HTTP requests for uploading and processing configurations.
     * <p>
     * This method extracts the configuration content from the request, saves it to a file,
     * clears any existing topic manager state, creates the new configuration, and returns
     * a visualization of the resulting graph.
     *
     * @param requestInfo Information about the HTTP request
     * @param clientOutput Output stream for sending the response to the client
     * @throws IOException If an I/O error occurs while processing the request
     */
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

    /**
     * Extracts the configuration content from the request body.
     *
     * @param requestInfo The request information containing the configuration content
     * @return The configuration content as a string
     */
    private String extractConfigContent(RequestParser.RequestInfo requestInfo) {
        return new String(requestInfo.getContent(), StandardCharsets.UTF_8);
    }

    /**
     * Saves the configuration content to a file.
     *
     * @param content The configuration content to save
     * @throws IOException If an error occurs while writing to the file
     */
    private void saveConfigToFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(UPLOADED_CONF_PATH)) {
            writer.write(content);
        }
    }

    /**
     * Clears the topic manager state.
     * <p>
     * This ensures that any previously loaded configuration does not interfere
     * with the new configuration.
     */
    private void clearTopicManager() {
        TopicManagerSingleton.get().clear();
    }

    /**
     * Creates the configuration from the saved file.
     * <p>
     * This method uses GenericConfig to parse the configuration file and
     * create the corresponding agents and topics.
     */
    private void createConfigFromFile() {
        GenericConfig config = new GenericConfig();
        config.setConfFile(UPLOADED_CONF_PATH);
        config.create();
    }

    /**
     * Generates an HTML visualization of the current state of the pub/sub graph.
     * <p>
     * This method also checks for cycles in the graph, which would indicate an
     * invalid configuration.
     *
     * @return HTML string representing the graph visualization
     * @throws IOException If an error occurs while generating the visualization
     * @throws RuntimeException If the configuration creates a cyclic graph
     */
    private String generateGraphVisualization() throws IOException {
        Graph graph = new Graph();
        graph.createFromTopics();

        if (graph.hasCycles()) {
            throw new RuntimeException("Invalid: The current configuration graph contains cycles. " +
                    "Please provide a non-cyclic configuration.");
        }

        return HtmlGraphWriter.getGraphHTML(graph);
    }

    /**
     * Sends the graph visualization as an HTTP response to the client.
     *
     * @param clientOutput Output stream for sending the response
     * @param graphHtml HTML content of the graph visualization
     * @throws IOException If an error occurs while sending the response
     */
    private void sendGraphResponse(OutputStream clientOutput, String graphHtml) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + graphHtml.length() + "\r\n" +
                "Connection: close\r\n\r\n" +
                graphHtml;
        clientOutput.write(response.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends an error response to the client when configuration processing fails.
     *
     * @param clientOutput Output stream for sending the response
     * @param errorMessage The error message to send
     * @throws IOException If an error occurs while sending the response
     */
    private void sendErrorResponse(OutputStream clientOutput, String errorMessage) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + errorMessage.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                errorMessage;
        clientOutput.write(response.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Cleans up resources when the servlet is closed.
     * <p>
     * Deletes the uploaded configuration file if it exists.
     *
     * @throws IOException If an error occurs while deleting the file
     */
    @Override
    public void close() throws IOException {
        Files.deleteIfExists(Paths.get(UPLOADED_CONF_PATH));
    }
}