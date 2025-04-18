package servlets;

import server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for HTTP request handlers in the web server.
 * <p>
 * Classes implementing this interface can be registered with the HTTP server
 * to handle specific URI patterns. Each servlet is responsible for processing
 * HTTP requests and generating appropriate responses for clients.
 * <p>
 * The server manages the lifecycle of servlets, creating them when needed and
 * closing them when they are removed or the server shuts down.
 */
public interface Servlet {

    /**
     * Handle a parsed request (RequestInfo) and write output to 'toClient'.
     * <p>
     * This method is called by the server when a request matching this servlet's
     * URI pattern is received. The servlet should process the request information
     * and write an appropriate HTTP response to the client's output stream.
     *
     * @param ri Information about the HTTP request to handle
     * @param toClient Output stream to write the HTTP response to
     * @throws IOException If an I/O error occurs while handling the request
     */
    void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException;

    /**
     * Called when shutting down the server or removing this Servlet.
     * <p>
     * This method allows the servlet to perform any necessary cleanup operations,
     * such as closing open resources, before it is destroyed.
     *
     * @throws IOException If an I/O error occurs during cleanup
     */
    void close() throws IOException;
}