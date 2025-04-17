package servlets;

import graph.TopicManagerSingleton;
import server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlLoader implements Servlet {

    private final String htmlDirectory;

    public HtmlLoader(String htmlDirectory) {
        this.htmlDirectory = htmlDirectory;
    }

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        String requestedFile = extractFileName(requestInfo.getUri());
        Path filePath = mapToFilePath(requestedFile);

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendFileNotFound(clientOutput, "File not found: " + requestedFile);
            return;
        }

        clearTopicManager();
        sendFileResponse(clientOutput, filePath);
    }

    private String extractFileName(String uri) {
        if (uri.equals("/app/")) {
            return "index.html";
        }

        return uri.substring("/app/".length());
    }

    private Path mapToFilePath(String fileName) {
        return Paths.get(htmlDirectory, fileName);
    }

    private void sendFileResponse(OutputStream clientOutput, Path filePath) throws IOException {
        sendResponseHeader(clientOutput);
        streamFileContent(clientOutput, filePath);
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

    private void sendFileNotFound(OutputStream clientOutput, String message) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Connection: close\r\n\r\n" +
                message;
        clientOutput.write(response.getBytes());
    }

    private void clearTopicManager() {
        TopicManagerSingleton.get().clear();
    }

    private void streamFileContent(OutputStream clientOutput, Path filePath) throws IOException {
        Files.copy(filePath, clientOutput);
    }

    @Override
    public void close() throws IOException {
    }
}