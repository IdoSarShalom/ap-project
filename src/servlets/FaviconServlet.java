package servlets;

import server.RequestParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A servlet that serves the favicon.ico file for the web application.
 * <p>
 * This servlet loads the favicon icon from a resource path and caches it in memory.
 * It responds to HTTP requests for the favicon by returning the cached icon bytes.
 * If the icon cannot be loaded, it returns a 404 Not Found response.
 * <p>
 * The servlet implements the Servlet interface and provides favicon serving capabilities
 * as part of the HTTP server.
 */
public class FaviconServlet implements Servlet {
    /** The resource path to the favicon icon file */
    private static final String ICON_PATH = "/servlets/favicon.ico";
    
    /** The cached bytes of the favicon icon */
    private byte[] iconBytes;

    /**
     * Creates a new favicon servlet and loads the favicon icon into memory.
     */
    public FaviconServlet() {
        loadIcon();
    }

    /**
     * Loads the favicon icon from the resource path.
     */
    private void loadIcon() {
        InputStream inputStream = getClass().getResourceAsStream(ICON_PATH);
        iconBytes = readIconBytes(inputStream);
    }

    /**
     * Reads the bytes of the favicon icon from an input stream.
     * <p>
     * This method reads the icon bytes in chunks and assembles them into a byte array.
     * If an I/O error occurs, an empty byte array is returned.
     *
     * @param inputStream The input stream to read from
     * @return The bytes of the favicon icon, or an empty byte array if reading fails
     */
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

    /**
     * Handles an HTTP request for the favicon.
     * <p>
     * If the favicon icon is available, returns it with a 200 OK response.
     * If the favicon could not be loaded, returns a 404 Not Found response.
     *
     * @param ri Information about the HTTP request
     * @param toClient Output stream to write the response to
     * @throws IOException If an I/O error occurs while writing the response
     */
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        if (iconBytes == null || iconBytes.length == 0) {
            writeResponse(toClient, 404, new byte[0]);

            return;
        }
        writeResponse(toClient, 200, iconBytes);
    }

    /**
     * Writes an HTTP response with the specified status code and content.
     * <p>
     * For a 200 status code, the content is sent as an image/x-icon.
     * For other status codes, the content is sent as text/plain.
     *
     * @param toClient Output stream to write the response to
     * @param statusCode The HTTP status code (200 for OK, 404 for Not Found)
     * @param content The content bytes to send in the response
     * @throws IOException If an I/O error occurs while writing
     */
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

    /**
     * Cleans up resources used by this servlet.
     * <p>
     * This implementation does not need to perform any cleanup.
     *
     * @throws IOException If an I/O error occurs during cleanup
     */
    public void close() throws IOException {
    }
}