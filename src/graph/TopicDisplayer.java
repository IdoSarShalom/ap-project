package graph;

import server.RequestParser;
import servlets.Servlet;

import java.io.IOException;
import java.io.OutputStream;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        publishMessage(ri);
        sendRedirectResponse(toClient);
    }

    private void publishMessage(RequestParser.RequestInfo ri) {
        String topic = ri.getParameters().get("topic");
        String message = ri.getParameters().get("message");
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(topic).publish(new Message(message));
    }

    private void sendRedirectResponse(OutputStream toClient) throws IOException {
        String response = buildRedirectResponse();
        toClient.write(response.getBytes());
    }

    private String buildRedirectResponse() {
        return "HTTP/1.1 303 See Other\r\n"
                + "Location: /app/\r\n"
                + "Connection: close\r\n\r\n";
    }

    @Override
    public void close() throws IOException {
    }
}