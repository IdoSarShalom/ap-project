package servlets;

import server.RequestParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FaviconServlet implements Servlet {
    private static final String ICON_PATH = "/servlets/favicon.ico";
    private byte[] iconBytes;

    public FaviconServlet() {
        loadIcon();
    }

    private void loadIcon() {
        InputStream inputStream = getClass().getResourceAsStream(ICON_PATH);
        iconBytes = readIconBytes(inputStream);
    }

    private byte[] readIconBytes(InputStream inputStream) {
        try (InputStream is = inputStream) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {

            return new byte[0];
        }
    }

    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        if (iconBytes == null || iconBytes.length == 0) {
            writeResponse(toClient, 404, new byte[0]);

            return;
        }
        writeResponse(toClient, 200, iconBytes);
    }

    private void writeResponse(OutputStream toClient, int statusCode, byte[] content) throws IOException {
        String statusLine = statusCode == 200 ? "HTTP/1.1 200 OK" : "HTTP/1.1 404 Not Found";
        String contentType = statusCode == 200 ? "image/x-icon" : "text/plain";
        String header = statusLine + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n\r\n";
        toClient.write(header.getBytes());
        toClient.write(content);
        toClient.flush();
    }

    public void close() throws IOException {
    }
}