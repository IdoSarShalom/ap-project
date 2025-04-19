package server;

import servlets.Servlet;

/**
 * Interface representing an HTTP server that can handle HTTP requests.
 * Implements the Runnable interface to allow the server to run in a separate thread.
 * This server can register and manage servlets for handling specific HTTP paths and methods.
 */
public interface HTTPServer extends Runnable {

    /**
     * Registers a servlet to handle requests for a specific HTTP method and URI path.
     *
     * @param httpCommand The HTTP method (e.g., "GET", "POST")
     * @param uri The URI path that this servlet will handle
     * @param s The servlet instance that will process matching requests
     */
    void addServlet(String httpCommand, String uri, Servlet s);

    /**
     * Removes a previously registered servlet for a specific HTTP method and URI path.
     *
     * @param httpCommand The HTTP method (e.g., "GET", "POST")
     * @param uri The URI path of the servlet to remove
     */
    void removeServlet(String httpCommand, String uri);

    /**
     * Starts the HTTP server, making it listen for incoming connections.
     */
    void start();

    /**
     * Stops the HTTP server and releases all resources.
     */
    void close();
}