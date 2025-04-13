package servlets;

import server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlLoader implements Servlet {
    private final String rootDirectory;

    public HtmlLoader(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    @Override
    public void handle(RequestParser.RequestInfo requestInfo, OutputStream clientOutput) throws IOException {
        String relativePath = getRelativePath(requestInfo.getUri());
        Path filePath = resolveFilePath(relativePath);
        writeResponse(clientOutput, filePath);
    }

    private String getRelativePath(String uri) {
        return uri.substring("/app/".length());
    }

    private Path resolveFilePath(String relativePath) {
        return Paths.get(rootDirectory, relativePath);
    }

    private void writeResponse(OutputStream clientOutput, Path filePath) throws IOException {
        writeResponseHeader(clientOutput);
        streamFileContent(clientOutput, filePath);
    }

    private void writeResponseHeader(OutputStream clientOutput) throws IOException {
        String header = createResponseHeader();
        clientOutput.write(header.getBytes());
    }

    private String createResponseHeader() {
        return "HTTP/1.1 200 OK\r\n"
                + "Connection: close\r\n\r\n";
    }

    private void streamFileContent(OutputStream clientOutput, Path filePath) throws IOException {
        Files.copy(filePath, clientOutput);
    }

    @Override
    public void close() throws IOException {
    }
}