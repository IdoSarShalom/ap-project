package servlets;

import server.RequestParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FaviconServlet implements Servlet {

    private byte[] iconBytes = null;
    private static final String ICON_PATH = "/graph/favicon.ico"; // Path relative to classpath root

    public FaviconServlet() {
        loadIcon();
    }

    private void loadIcon() {
        try (InputStream is = getClass().getResourceAsStream(ICON_PATH)) {
            if (is == null) {
                System.err.println("ERROR: Favicon resource not found at: " + ICON_PATH);
                iconBytes = new byte[0]; // Set to empty if not found
                return;
            }

            // Read all bytes from the input stream
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            iconBytes = buffer.toByteArray();
            System.out.println("Favicon loaded successfully (" + iconBytes.length + " bytes).");

        } catch (IOException e) {
            System.err.println("ERROR: Failed to load favicon resource: " + e.getMessage());
            iconBytes = new byte[0]; // Set to empty on error
            e.printStackTrace();
        }
    }

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        if (iconBytes == null || iconBytes.length == 0) {
            // Send a 404 Not Found if the icon wasn't loaded
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n\r\n";
            toClient.write(response.getBytes());
            toClient.flush();
            System.out.println("Favicon requested but not found or loaded.");
            return;
        }

        // Send the icon with correct headers
        String header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: image/x-icon\r\n" +
                "Content-Length: " + iconBytes.length + "\r\n" +
                "Connection: close\r\n\r\n";

        toClient.write(header.getBytes());
        toClient.write(iconBytes);
        toClient.flush();
        // System.out.println("Favicon served."); // Optional: uncomment for verbose logging
    }

    @Override
    public void close() throws IOException {
        // Nothing to close for this servlet
    }
}
