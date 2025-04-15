package graph;

import server.RequestParser;
import servlets.Servlet;

import java.io.IOException;
import java.io.OutputStream;

public class EmptyLoader implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        System.out.println("[EmptyLoader] START - Handling " + requestInfo.getHttpCommand() + " request to " + requestInfo.getUri());

        // Super simple static response - just a basic message
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 7\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                "SUCCESS";

        // Write the entire response at once
        clientOutput.write(response.getBytes());

        // Force flush the output stream
        clientOutput.flush();

        System.out.println("[EmptyLoader] END - Response sent");
    }

    @Override
    public void close() throws IOException {
        // Nothing to close
    }
}