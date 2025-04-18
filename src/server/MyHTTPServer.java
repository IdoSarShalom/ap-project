package server;

import servlets.Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete implementation of the HTTPServer interface that handles HTTP requests.
 * <p>
 * This class provides a multi-threaded HTTP server that can handle GET, POST, and DELETE
 * requests by dispatching them to registered servlets. It maintains maps of URI patterns
 * to servlet instances for each HTTP method, and uses a thread pool to handle client
 * connections concurrently.
 */
public class MyHTTPServer extends Thread implements HTTPServer {
    /** Logger for this class */
    private static final Logger logger = Logger.getLogger(MyHTTPServer.class.getName());

    /** The port to listen on */
    private final int port;
    
    /** The number of threads in the thread pool */
    private final int nThreads;
    
    /** Map of URI patterns to servlets for GET requests */
    private final ConcurrentHashMap<String, Servlet> getUriToServletMap;
    
    /** Map of URI patterns to servlets for POST requests */
    private final ConcurrentHashMap<String, Servlet> postUriToServletMap;
    
    /** Map of URI patterns to servlets for DELETE requests */
    private final ConcurrentHashMap<String, Servlet> deleteUriToServletMap;
    
    /** Flag indicating whether the server is running */
    private volatile boolean running;
    
    /** The server socket that listens for connections */
    private ServerSocket serverSocket;
    
    /** The thread pool for handling client connections */
    private ExecutorService executor;

    /**
     * Creates a new HTTP server with the specified port and thread pool size.
     *
     * @param port The port to listen on
     * @param maxThreads The maximum number of threads in the thread pool
     */
    public MyHTTPServer(int port, int maxThreads) {
        super("MyHTTPServer-MainThread");
        this.port = port;
        this.nThreads = maxThreads;
        this.running = false;
        this.getUriToServletMap = new ConcurrentHashMap<>();
        this.postUriToServletMap = new ConcurrentHashMap<>();
        this.deleteUriToServletMap = new ConcurrentHashMap<>();
        this.serverSocket = null;
        this.executor = null;
    }

    /**
     * Registers a servlet to handle requests for a specific HTTP method and URI path.
     *
     * @param httpCommand The HTTP method (e.g., "GET", "POST")
     * @param uri The URI path that this servlet will handle
     * @param s The servlet instance that will process matching requests
     */
    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand.toUpperCase());
        if (servletMap != null) {
            servletMap.put(uri, s);
            logger.log(Level.CONFIG, "Added servlet for {0} {1}: {2}", new Object[]{httpCommand, uri, s.getClass().getName()});
        } else {
            logger.log(Level.WARNING, "Attempted to add servlet for unsupported HTTP command: {0}", httpCommand);
        }
    }

    /**
     * Removes a previously registered servlet for a specific HTTP method and URI path.
     *
     * @param httpCommand The HTTP method (e.g., "GET", "POST")
     * @param uri The URI path of the servlet to remove
     */
    @Override
    public void removeServlet(String httpCommand, String uri) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand.toUpperCase());
        if (servletMap != null) {
            closeAndRemoveServlet(servletMap, uri);
            logger.log(Level.CONFIG, "Removed servlet for {0} {1}", new Object[]{httpCommand, uri});
        } else {
            logger.log(Level.WARNING, "Attempted to remove servlet for unsupported HTTP command: {0}", httpCommand);
        }
    }

    /**
     * Starts the HTTP server, making it listen for incoming connections.
     * <p>
     * This method initializes the server and starts the main thread if the server
     * is not already running.
     */
    @Override
    public void start() {
        if (!running) {
            initializeServer();
            super.start();
            logger.log(Level.INFO, "MyHTTPServer started on port {0} with {1} threads.", new Object[]{port, nThreads});
        } else {
            logger.log(Level.WARNING, "Server start() called when already running.");
        }
    }

    /**
     * Main thread method that accepts client connections and processes HTTP requests.
     * <p>
     * This method creates a server socket, listens for client connections, and
     * dispatches them to the thread pool for processing.
     */
    @Override
    public void run() {
        try (ServerSocket ss = createServerSocket()) {
            logger.log(Level.INFO, "Server socket listening on port {0}", port);
            acceptClientConnections(ss);
        } catch (IOException e) {
            if (running) {
                logger.log(Level.SEVERE, "Server socket error in main loop, shutting down.", e);
            }
        } finally {
            logger.info("Server main loop exiting.");
            closeExecutor();
        }
    }

    /**
     * Stops the HTTP server and releases all resources.
     * <p>
     * This method stops the server, closes all registered servlets, and
     * shuts down the thread pool.
     */
    @Override
    public void close() {
        logger.info("Server shutdown requested.");
        stopServer();
        closeAllServlets();
        closeExecutor();
        logger.info("Server shutdown complete.");
    }

    /**
     * Handles a client connection by processing its HTTP request.
     * <p>
     * This method is called by the thread pool for each client connection.
     * It reads the request, finds the appropriate servlet, and dispatches
     * the request to that servlet.
     *
     * @param client The client socket to handle
     */
    private void handleClient(Socket client) {
        logger.log(Level.FINE, "Connection received from: {0}", client.getRemoteSocketAddress());
        try (Socket c = client;
             BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
             OutputStream out = c.getOutputStream()) {
            processClientRequest(br, out);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Empty or null request line")) {
                logger.log(Level.INFO, "Client connected but sent no data or closed connection early: {0}", client.getRemoteSocketAddress());
            } else {
                logger.log(Level.WARNING, "IOException during client handling for {0}: {1}",
                        new Object[]{client.getRemoteSocketAddress(), e.getMessage()});
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during client handling for " + client.getRemoteSocketAddress(), e);
        }
        logger.log(Level.FINE, "Finished handling connection from: {0}", client.getRemoteSocketAddress());
    }

    /**
     * Gets the servlet map for a specific HTTP method.
     *
     * @param httpCommand The HTTP method (e.g., "GET", "POST", "DELETE")
     * @return The map of URI patterns to servlets for the specified method, or null if the method is not supported
     */
    private Map<String, Servlet> getServletMap(String httpCommand) {
        return switch (httpCommand) {
            case "GET" -> getUriToServletMap;
            case "POST" -> postUriToServletMap;
            case "DELETE" -> deleteUriToServletMap;
            default -> null;
        };
    }

    /**
     * Closes and removes a servlet from a servlet map.
     *
     * @param servletMap The map containing the servlet
     * @param uri The URI pattern of the servlet to remove
     */
    private void closeAndRemoveServlet(Map<String, Servlet> servletMap, String uri) {
        Servlet servlet = servletMap.remove(uri);
        if (servlet != null) {
            try {
                servlet.close();
                logger.log(Level.FINE, "Closed servlet for URI: {0}", uri);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing servlet during removal for URI: " + uri, e);
            }
        }
    }

    /**
     * Initializes the server by setting the running flag and creating the thread pool.
     */
    private void initializeServer() {
        running = true;
        executor = Executors.newFixedThreadPool(nThreads);
        logger.fine("Server initialized, thread pool created.");
    }

    /**
     * Creates a server socket on the configured port with a timeout.
     *
     * @return The created server socket
     * @throws IOException If an I/O error occurs while creating the socket
     */
    private ServerSocket createServerSocket() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000);
        return serverSocket;
    }

    /**
     * Accepts client connections and dispatches them to the thread pool.
     *
     * @param ss The server socket to accept connections from
     */
    private void acceptClientConnections(ServerSocket ss) {
        logger.info("Server ready to accept connections...");
        while (running) {
            try {
                Socket client = ss.accept();
                executor.submit(() -> handleClient(client));
            } catch (SocketTimeoutException e) {
                // Timeout occurred - just loop again to check if we're still running
            } catch (IOException e) {
                if (running) {
                    logger.log(Level.WARNING, "IOException accepting client connection", e);
                }
            }
        }
        logger.info("Stopped accepting connections.");
    }

    /**
     * Processes an HTTP request from a client.
     * <p>
     * This method parses the request, finds the appropriate servlet, and dispatches
     * the request to that servlet. If no servlet is found, it sends a 404 response.
     * If an error occurs during processing, it sends an appropriate error response.
     *
     * @param br The BufferedReader to read the request from
     * @param out The OutputStream to write the response to
     * @throws IOException If an I/O error occurs while processing the request
     */
    private void processClientRequest(BufferedReader br, OutputStream out) throws IOException {
        RequestParser.RequestInfo ri = null;
        String requestIdentifier = "Unknown Request";
        try {
            ri = RequestParser.parseRequest(br);
            requestIdentifier = ri.getHttpCommand() + " " + ri.getUri();
            logger.log(Level.FINE, "Processing request: {0}", requestIdentifier);
        } catch (IOException e) {
            if (e.getMessage() == null || !e.getMessage().contains("Empty or null request line")) {
                logger.log(Level.WARNING, "IOException during request parsing: {0}", e.getMessage());
                try {
                    writeBadRequest(out, "Malformed request reading failed");
                } catch (IOException ioe) {
                    logger.log(Level.WARNING, "Failed to send 400 error response.", ioe);
                }
                return;
            } else {
                throw e;
            }
        }

        Servlet servlet = findServlet(ri.getHttpCommand(), ri.getResourceUri());

        if (servlet == null) {
            logger.log(Level.INFO, "No servlet found for request: {0}", requestIdentifier);
            try {
                writeNotFound(out, "No servlet for " + ri.getHttpCommand() + " " + ri.getResourceUri());
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Failed to send 404 error response.", ioe);
            }
            return;
        }

        try {
            logger.log(Level.FINE, "Dispatching request {0} to servlet {1}",
                    new Object[]{requestIdentifier, servlet.getClass().getName()});
            servlet.handle(ri, out);
            logger.log(Level.FINE, "Servlet {0} finished handling request {1}",
                    new Object[]{servlet.getClass().getName(), requestIdentifier});
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing servlet " + servlet.getClass().getName() + " for request " + requestIdentifier, e);
            try {
                if (out != null) {
                    writeInternalError(out, "Servlet execution failed");
                }
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Failed to send 500 error response after servlet error.", ioe);
            }
        }
    }

    /**
     * Finds the servlet that should handle a request.
     * <p>
     * This method finds the servlet registered for the given HTTP method and URI path.
     * It uses a longest prefix match to find the most specific servlet.
     *
     * @param httpCommand The HTTP method of the request
     * @param uri The URI path of the request
     * @return The servlet that should handle the request, or null if none is found
     */
    private Servlet findServlet(String httpCommand, String uri) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand);
        return matchServletToUri(uri, servletMap);
    }

    /**
     * Matches a URI to a servlet using longest prefix matching.
     *
     * @param uri The URI to match
     * @param uriToServlet The map of URI patterns to servlets
     * @return The servlet with the longest matching prefix, or null if none is found
     */
    private Servlet matchServletToUri(String uri, Map<String, Servlet> uriToServlet) {
        Servlet matchingServlet = null;
        int longestPrefixLength = -1;
        String matchedUri = null;

        if (uriToServlet == null) return null;

        for (String currentUri : uriToServlet.keySet()) {
            if (uri.startsWith(currentUri) && currentUri.length() > longestPrefixLength) {
                longestPrefixLength = currentUri.length();
                matchingServlet = uriToServlet.get(currentUri);
                matchedUri = currentUri;
            }
        }
        if (matchingServlet != null) {
            logger.log(Level.FINER, "Matched URI {0} to servlet pattern {1}", new Object[]{uri, matchedUri});
        }
        return matchingServlet;
    }

    /**
     * Writes a 400 Bad Request response to the client.
     *
     * @param out The OutputStream to write to
     * @param msg The error message to include in the response
     * @throws IOException If an I/O error occurs while writing the response
     */
    private void writeBadRequest(OutputStream out, String msg) throws IOException {
        String resp = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                msg;
        out.write(resp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Writes a 404 Not Found response to the client.
     *
     * @param out The OutputStream to write to
     * @param msg The error message to include in the response
     * @throws IOException If an I/O error occurs while writing the response
     */
    private void writeNotFound(OutputStream out, String msg) throws IOException {
        String resp = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                msg;
        out.write(resp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Writes a 500 Internal Server Error response to the client.
     *
     * @param out The OutputStream to write to
     * @param msg The error message to include in the response
     * @throws IOException If an I/O error occurs while writing the response
     */
    private void writeInternalError(OutputStream out, String msg) throws IOException {
        String resp = "HTTP/1.1 500 Internal Server Error\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + msg.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + "\r\n" +
                "Connection: close\r\n\r\n" +
                msg;
        out.write(resp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Stops the server by setting the running flag to false and closing the server socket.
     */
    private void stopServer() {
        running = false;
        logger.fine("Setting running flag to false.");
        if (serverSocket != null) {
            try {
                serverSocket.close();
                logger.info("Server socket closed.");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing server socket", e);
            }
        }
    }

    /**
     * Closes all registered servlets.
     */
    private void closeAllServlets() {
        logger.fine("Closing all registered servlets...");
        for (Map<String, Servlet> servletMap : new Map[]{getUriToServletMap, postUriToServletMap, deleteUriToServletMap}) {
            if (servletMap != null) {
                for (Servlet servlet : servletMap.values()) {
                    try {
                        servlet.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Error closing servlet " + servlet.getClass().getName(), e);
                    }
                }
            }
        }
        logger.fine("Finished closing servlets.");
    }

    /**
     * Closes the executor service by shutting it down gracefully or forcibly.
     */
    private void closeExecutor() {
        if (executor != null) {
            logger.fine("Shutting down executor service...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warning("Executor did not terminate gracefully after 5 seconds, forcing shutdown.");
                    executor.shutdownNow();
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        logger.severe("Executor did not terminate even after forced shutdown.");
                    }
                }
                logger.info("Executor service shut down.");
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Interrupted while waiting for executor shutdown", e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}