package graph;

import server.RequestParser;
import servlets.Servlet;

import java.io.IOException;
import java.io.OutputStream;

public class EmptyLoader implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        sendResponseHeader(clientOutput);
    }

    private void sendResponseHeader(OutputStream clientOutput) throws IOException {
        String header = buildResponseHeader();
        clientOutput.write(header.getBytes());
    }

    private String buildResponseHeader() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n";
    }

    @Override
    public void close() throws IOException {
    }
}