package graph;

import server.RequestParser;
import servlets.Servlet;

import java.io.IOException;
import java.io.OutputStream;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        publishMessage(ri);
        sendResponse(toClient);
    }

    private void publishMessage(RequestParser.RequestInfo ri) {
        String topic = ri.getParameters().get("topic");
        String message = ri.getParameters().get("message");
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        tm.getTopic(topic).publish(new Message(message));
    }

    private void sendResponse(OutputStream toClient) throws IOException {

        // TODO: invoke HTMLLoader perhaps after calculating the graph via HtmlGraphWriter
        // Do this by invoking /app/ with the corresponding file e.g. index.html
        // after preparing this file for the user


        String htmlResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n" +
                "<html><body><h1>Message Published</h1>" +
                "<a href='/app/'>Go back to app</a>" +
                "</body></html>";

        toClient.write(htmlResponse.getBytes());
    }

    private String buildRedirectResponse() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n";
    }

    @Override
    public void close() throws IOException {
    }
}