package graph;

import server.RequestParser;
import servlets.Servlet;
import views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Servlet responsible for handling publish requests to topics and displaying 
 * the resulting graph visualization.
 * <p>
 * This servlet receives topic and message parameters, publishes the message to
 * the specified topic, and then generates and returns an HTML visualization of
 * the pub/sub system's current state.
 */
public class TopicDisplayer implements Servlet {

    /**
     * Handles HTTP requests for publishing messages to topics.
     * <p>
     * The method extracts the topic and message from the request parameters,
     * publishes the message to the topic, generates a visualization of the
     * pub/sub system, and sends it back to the client.
     *
     * @param requestInfo Information about the HTTP request
     * @param clientOutput Output stream for sending the response to the client
     * @throws IOException If an I/O error occurs while processing the request
     */
    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        publishMessage(requestInfo);
        String graphHtml = generateGraphVisualization();
        sendGraphResponse(clientOutput, graphHtml);
    }

    /**
     * Publishes a message to a topic based on the parameters in the request.
     *
     * @param requestInfo The request information containing topic and message parameters
     */
    private void publishMessage(RequestParser.RequestInfo requestInfo) {
        String topic = requestInfo.getParameters().get("topic");
        String message = requestInfo.getParameters().get("message");
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        topicManager.getTopic(topic).publish(new Message(message));
    }

    /**
     * Generates an HTML visualization of the current state of the pub/sub graph.
     *
     * @return HTML string representing the graph visualization
     * @throws IOException If an error occurs while generating the visualization
     */
    private String generateGraphVisualization() throws IOException {
        Graph graph = new Graph();
        graph.createFromTopics();
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
     * Closes any resources used by this servlet.
     * <p>
     * This implementation doesn't need to release any resources.
     *
     * @throws IOException If an error occurs while closing resources
     */
    @Override
    public void close() throws IOException {
    }
}