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
        generateGraphVisualization();
        sendResponse(clientOutput);
    }

    private void publishMessage(RequestParser.RequestInfo requestInfo) {
        String topic = requestInfo.getParameters().get("topic");
        String message = requestInfo.getParameters().get("message");
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        topicManager.getTopic(topic).publish(new Message(message));
    }

    private void generateGraphVisualization() throws IOException {
        Graph graph = new Graph();
        graph.createFromTopics();
        HtmlGraphWriter.getGraphHTML(graph, GRAPH_HTML_PATH);
    }

    private void sendResponse(OutputStream clientOutput) throws IOException {
        String redirectResponse = "HTTP/1.1 302 Found\r\n" +
                "Location: /app/index.html\r\n" +
                "Connection: close\r\n\r\n";
        clientOutput.write(redirectResponse.getBytes());
    }

    @Override
    public void close() throws IOException {
    }
}