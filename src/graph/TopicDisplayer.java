package graph;

import server.RequestParser;
import servlets.Servlet;
import views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;

public class TopicDisplayer implements Servlet {
    private static final String GRAPH_HTML_PATH = "web/graph.html";

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        publishMessage(requestInfo);
        String graphHtml = generateGraphVisualization();
        sendGraphResponse(clientOutput, graphHtml);
    }

    private void publishMessage(RequestParser.RequestInfo requestInfo) {
        String topic = requestInfo.getParameters().get("topic");
        String message = requestInfo.getParameters().get("message");
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        topicManager.getTopic(topic).publish(new Message(message));
    }

    private String generateGraphVisualization() throws IOException {
        Graph graph = new Graph();
        graph.createFromTopics();
        return HtmlGraphWriter.getGraphHTML(graph);
    }

    private void sendGraphResponse(OutputStream clientOutput, String graphHtml) throws IOException {
        String header = buildResponseHeader(graphHtml);
        clientOutput.write(header.getBytes());
    }

    private String buildResponseHeader(String graphHtml) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n" +
                graphHtml;
    }

    @Override
    public void close() throws IOException {
    }
}