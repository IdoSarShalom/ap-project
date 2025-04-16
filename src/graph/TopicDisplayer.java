package graph;

import server.RequestParser;
import servlets.Servlet;
import views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TopicDisplayer implements Servlet {

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
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + graphHtml.length() + "\r\n" +
                "Connection: close\r\n\r\n" +
                graphHtml;
        clientOutput.write(response.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
    }
}