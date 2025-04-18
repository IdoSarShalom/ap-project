package servlets;

import graph.TopicManagerSingleton;
import server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A servlet that loads and serves HTML files from a specified directory.
 * <p>
 * This servlet handles HTTP requests by loading HTML files from a configured 
 * directory and returning them to the client. It supports serving the index.html
 * file when the root URI is requested, and clears the topic manager before
 * serving each file to ensure a clean pub/sub system state.
 * <p>
 * The servlet implements the Servlet interface and provides file loading capabilities
 * as part of the HTTP server.
 */
public class HtmlLoader implements Servlet {

    /** The directory containing HTML files to serve */
    private final String htmlDirectory;

    /**
     * Creates a new HTML loader servlet with the specified HTML directory.
     *
     * @param htmlDirectory The directory path containing HTML files to serve
     */
    public HtmlLoader(String htmlDirectory) {
        this.htmlDirectory = htmlDirectory;
    }

    /**
     * Handles an HTTP request by loading and returning the requested HTML file.
     * <p>
     * Extracts the requested filename from the URI, maps it to the corresponding
     * file path, and sends the file content to the client if it exists. If the file
     * does not exist or is a directory, a 404 response is sent.
     *
     * @param requestInfo Information about the HTTP request
     * @param clientOutput Output stream to write the response to
     * @throws IOException If an I/O error occurs while reading or writing
     */
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

    /**
     * Extracts the filename from the URI.
     * <p>
     * If the URI is the root path ("/app/"), returns "index.html",
     * otherwise extracts the filename from the URI.
     *
     * @param uri The URI from the HTTP request
     * @return The extracted filename
     */
    private String extractFileName(String uri) {
        if (uri.equals("/app/")) {
            return "index.html";
        }

        return uri.substring("/app/".length());
    }

    /**
     * Maps a filename to its absolute path in the HTML directory.
     *
     * @param fileName The name of the file to map
     * @return The absolute path to the file
     */
    private Path mapToFilePath(String fileName) {
        return Paths.get(htmlDirectory, fileName);
    }

    /**
     * Sends a file response to the client.
     * <p>
     * Writes the HTTP header and streams the file content to the client.
     *
     * @param clientOutput Output stream to write the response to
     * @param filePath Path to the file to send
     * @throws IOException If an I/O error occurs while reading or writing
     */
    private void sendFileResponse(OutputStream clientOutput, Path filePath) throws IOException {
        sendResponseHeader(clientOutput);
        streamFileContent(clientOutput, filePath);
    }

    /**
     * Sends the HTTP response header to the client.
     *
     * @param clientOutput Output stream to write the header to
     * @throws IOException If an I/O error occurs while writing
     */
    private void sendResponseHeader(OutputStream clientOutput) throws IOException {
        String header = buildResponseHeader();
        clientOutput.write(header.getBytes());
    }

    /**
     * Builds the HTTP response header for successful file responses.
     *
     * @return The HTTP response header string
     */
    private String buildResponseHeader() {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n";
    }

    /**
     * Sends a 404 File Not Found response to the client.
     *
     * @param clientOutput Output stream to write the response to
     * @param message Error message to include in the response
     * @throws IOException If an I/O error occurs while writing
     */
    private void sendFileNotFound(OutputStream clientOutput, String message) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Connection: close\r\n\r\n" +
                message;
        clientOutput.write(response.getBytes());
    }

    /**
     * Clears the topic manager to ensure a clean pub/sub system state.
     */
    private void clearTopicManager() {
        TopicManagerSingleton.get().clear();
    }

    /**
     * Streams the content of a file to the client.
     *
     * @param clientOutput Output stream to write the file content to
     * @param filePath Path to the file to stream
     * @throws IOException If an I/O error occurs while reading or writing
     */
    private void streamFileContent(OutputStream clientOutput, Path filePath) throws IOException {
        Files.copy(filePath, clientOutput);
    }

    /**
     * Cleans up resources used by this servlet.
     * <p>
     * This implementation does not need to perform any cleanup.
     *
     * @throws IOException If an I/O error occurs during cleanup
     */
    @Override
    public void close() throws IOException {
    }
}